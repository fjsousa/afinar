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


(defn reference-depth [ref]
  (count (filter uuid? ref)))

#_(reference-depth [nil
                  #uuid "0157e763-ea43-4944-ae04-723db85571df"
                  #uuid "2b486994-e108-427e-87ad-a841521d9609"
                  :onesearch-index-builder.service.property-service/process-batch])

(defn get-all-children
  "get children or returns nil"
  [v v-list]
  (let [ref-uuid-fn #(->> % (filter uuid?))
        ref (ref-uuid-fn v)
        c (count ref)]
    (not-empty (filter (fn [[curr-v _]]
                         (and (= ref (take c (ref-uuid-fn curr-v)))
                              (not (= ref (ref-uuid-fn curr-v) )))) v-list))))

(defn skim-top-level [l]
  (when l
    (let [c (-> l ffirst count)]
     (filter #(= c (-> % first count)) l))))

#_(skim-top-level
 (get-all-children [nil
                    #uuid "aa4231a7-8468-4d46-91d0-99a176e68ea2"
                    #uuid "2b486994-e108-427e-87ad-a841521d9609"
                    #uuid "8d599924-3ae7-4582-95d7-751e0251559a"
                    :onesearch-index-builder.service.property-service/process-units-required]
                   sorted-list-by-level))


;; 5 - if there are children, put them into the vector


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


(ref->unique-fn-name
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
  [v-list tt]
  (let [top-level (-> v-list skim-top-level)]
    (map (fn [[each-top-level-ref each-top-level-time]]
           (let [children (get-all-children each-top-level-ref v-list)
                 fn-unique-name (ref->unique-fn-name each-top-level-ref)
                 percentage (calc-percentage each-top-level-time tt)
                 children-time (apply + (map last (skim-top-level children)))
                 time-other (calc-percentage (- each-top-level-time children-time) tt)]
             (if children
               (into
                [fn-unique-name percentage
                   ["other" time-other]] (sort-the-rest children tt))
               [fn-unique-name
                percentage]))) top-level)))

(defn postprocess-2
  [postprocessed]
  (let [sorted-list-by-depth
        (sort-by #(-> % first reference-depth) postprocessed)
        top-level-data (first sorted-list-by-depth)
        total-time (-> top-level-data last)
        final-output (into [(-> top-level-data first ref->unique-fn-name)
                            (secs->min total-time)]
                           (sort-the-rest (rest sorted-list-by-depth)
                                          total-time))]
    final-output))

(comment (clojure.pprint/pprint (postprocess-2 postprocessed))
 (postprocess-2 postprocessed))

(defn postprocess
  []
  (postprocess-2 (->> (group-by first @times)
                      (map (fn [[ref everything]]
                             [ref (->> everything
                                       (map last)
                                       (reduce +))])))))


#_(ddiff/pretty-print  (ddiff/diff result-d65de4e results-batch-25))
