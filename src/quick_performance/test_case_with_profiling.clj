(ns quick-performance.test-case-with-profiling
  (:require [quick-performance.core :refer [ctx defn-t times]]))

(defn-t C [data-partition]
  (println "Function C called with" (count data-partition) "elements")
  (Thread/sleep 500))

(defn-t D [x]
  (println "Function D called with" x)
  (Thread/sleep 1000) ; Sleep for 1 second
  )

(defn-t B [data-partition arg]
  (if (= arg :foo)
    (ctx (C data-partition))
    (ctx (doseq [x data-partition]
       (D x)))))

(defn-t A [data]
  (doseq [data-partition (partition-all 5 data)] ; Partition the data into chunks of 5
    (ctx (doseq [x data-partition]
       (D x))) ; Call function D on each item in the partition
    (ctx (B data-partition :foo)) ; Call function B twice on the first item in the partition

    (ctx (B data-partition :bar))))

;; +---------+       +---------+
;; |    A    | ->    |    B    |
;; +---------+     / +---------+
;;       |        /       |
;;       v       /        v
;; +---------+  /    +---------+
;; |    D    | <     |    C    |
;; +---------+       +---------+

(defn run
  "I don't do a whole lot."
  [& _]
  (A (range 20))
  (clojure.pprint/pprint @times))

(run)
