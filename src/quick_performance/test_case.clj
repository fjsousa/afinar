(ns quick-performance.core)

(defn C [data-partition]
  (println "Function C called with" (count data-partition) "elements")
  (Thread/sleep 500))

(defn D [x]
  (println "Function D called with" x)
  (Thread/sleep 1000) ; Sleep for 1 second
  )

(defn B [data-partition arg]
  (if (= arg :foo)
    (C data-partition)
    (doseq [x data-partition]
      (D x))))

(defn A [data]
  (doseq [data-partition (partition-all 5 data)] ; Partition the data into chunks of 5
    (doseq [x data-partition]
            (D x)) ; Call function D on each item in the partition
    (B data-partition :foo) ; Call function B twice on the first item in the partition

    (B data-partition :bar)))

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
  [_]
  (A (range 20)))
