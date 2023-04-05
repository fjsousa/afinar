(ns quick-performance.test-case-with-profiling
  (:require [quick-performance.core :refer [ctx defn-t times]]))

(defn-t C [data-partition]
  (Thread/sleep 500))

(defn-t D [x]
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


;; this happens twice

;; A -> D x5

;; D 5 secs
;; A 5 secs

;; foo branch
;; A -> B -> C

;; C .5 secs
;; B .5 secs
;; A .5 secs

;; bar branch
;; A -> B -> D x5

;; D 5 secs
;; B 5 secs
;; A 5 secs

;;
;; A      = 2 x (5 + .5 + 5) = 21 secs
;; B foo  = 2 x .5 = 1 sec
;; B bar  = 2 x 5 = 10 secs
;; C      = 2 x (.5) = 1 sec
;; D main = 2 x 5 = 10 secs
;; D bar  = 2 x 5 = 10 secs

(defn run
  [& _]
  (reset! times [])
  (A (range 10))
  (clojure.pprint/pprint @times))
