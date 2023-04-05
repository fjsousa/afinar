(ns quick-performance.core
  (:require [clojure.string :as str]
            [lambdaisland.deep-diff2 :as ddiff]))

(def ^:dynamic context-uuid [nil])
(def times (atom []))

(defn get-nano-now
  "now in nanoseconds"
  []
  (. System (nanoTime)))

(defn elapsed-time-mili
  "start is in nanoseconds"
  [start]
  (/ (double (- (get-nano-now) start)) 1000000.0))

(defn pretty-fn-name
  "*ns* and function name as string. returns:

  :name.space/function-name"
  [ns fn-name]
  (keyword (str ns "/" fn-name)))

(defmacro defn-t [name args & body]
  `(def ~name
     (fn ~args
       (let [start# (get-nano-now)
             result# ~(reverse (into (list 'do) body))
             time-mili# (elapsed-time-mili start#)
             fn-name# ~(pretty-fn-name *ns* (str name))]
         #_(log/info :profile-debug :time-mili time-mili#)
         (swap! quick-performance.core/times
                conj
                [(conj quick-performance.core/context-uuid
                  fn-name#)
                 time-mili#])
         result#))))

(defmacro ctx [body]
  `(binding [quick-performance.core/context-uuid
             (conj quick-performance.core/context-uuid ~(java.util.UUID/randomUUID))]
    ~body))


(defn get-all-children
  "get children or returns nil"
  [parent-ref ordered-list]
  (let [ref-uuid-fn #(->> % (filter uuid?))
        parent-ref-uuids (ref-uuid-fn parent-ref)
        c (count parent-ref-uuids)]
    (not-empty
     (filter (fn [[curr-ref _]]
               (and (= parent-ref-uuids (take c (ref-uuid-fn curr-ref)))
                    (not (= parent-ref-uuids (ref-uuid-fn curr-ref))))) ordered-list))))

(defn skim-top-level [ordered-list]
  (when ordered-list
    (let [c (-> ordered-list ffirst count)]
     (filter #(= c (-> % first count)) ordered-list))))

(defn format-2dec
  [v]
  (java.lang.Double/valueOf (str/replace (format "%.2f" v) "," ".")))

#_(format-percent 1.2222222)

(defn ref->unique-fn-name [ref]
  (let [context-ref (-> ref rest)
        fn-name (last context-ref)
        context-uuids (drop-last context-ref)
        drop-app-name (rest (str/split (namespace fn-name) #"\."))
        fn-name (str (str/join "." (map first drop-app-name) ) "/" (name fn-name))]
    fn-name
    #_(apply str (into [fn-name "-"]
                     (->> context-uuids
                          (map #(->> % str (take 2) (apply str))))))))


#_(ref->unique-fn-name
 [nil
  #uuid "0157e763-ea43-4944-ae04-723db85571df"
  #uuid "2b486994-e108-427e-87ad-a841521d9609"
  :onesearch-index-builder.service.property-service/process-batch])

(defn calc-percentage [t tt]
  (format-2dec (* 100 (/ t tt))))

(defn secs->min
  [time-secs]
  (format-2dec (/ (/ time-secs 1000) 60)))

(defn sort-the-rest
  [ordered-list total-exec-time]
  (let [top-level (skim-top-level ordered-list)]
    (map (fn [[each-top-level-ref each-top-level-time]]
           (let [children (get-all-children each-top-level-ref ordered-list)
                 fn-unique-name (ref->unique-fn-name each-top-level-ref)
                 percentage (calc-percentage each-top-level-time total-exec-time)
                 children-time (apply + (map last (skim-top-level children)))
                 time-other (calc-percentage (- each-top-level-time children-time) total-exec-time)]
             (if children
               (into
                [fn-unique-name percentage
                   ["other" time-other]] (sort-the-rest children total-exec-time))
               [fn-unique-name
                percentage]))) top-level)))

(defn reference-depth [ref]
  (count (filter uuid? ref)))

(defn order-by-depth [grouped-times]
  (sort-by #(-> % first reference-depth) grouped-times))

(defn sum-times-and-group-by-ref-ordered
  "sums all times and groups by reference preserving order"
  [times]
  (order-by-depth
   (loop [[[ref time] & rest-times] times
          aux-ref-map {}
          result []]
     (cond
       (not ref) result

       (aux-ref-map ref)
       (recur rest-times
              aux-ref-map
              (update-in result [(get aux-ref-map ref) 1] + time))
       :else (recur rest-times
                    (assoc aux-ref-map ref (count aux-ref-map))
                    (conj result [ref time]))))))


(defn post-process
  []
  (let [ordered-times (sum-times-and-group-by-ref-ordered @times)
        top-level-data (first ordered-times)
        total-time (-> top-level-data last)
        final-output (into [(-> top-level-data first ref->unique-fn-name)
                            (secs->min total-time)]
                           (sort-the-rest (rest ordered-times)
                                          total-time))]
    final-output))



#_(comment (clojure.pprint/pprint (postprocess-2 postprocessed))
           (postprocess-2 postprocessed))

#_(ddiff/pretty-print  (ddiff/diff result-d65de4e results-batch-25))
