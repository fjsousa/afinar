Just throwing some ideas in here for a clojure performance library. In my last gig, we had a clojure process that would run every day, pull data from a bunch of microservices with different batch size configuration and  build an Elastic Search index. With each new feature added, the process would take more time to complete, and although not critical for the business, we were starting to feel uncomfortable. The team had an intuition about which parts were taking longer and how to teak things, but I wanted an easy way to instrument the code in clojure and track changes. This is what I envisioned.

## The problem

I wanted a way to track completion times for critical functions within the process/job which preserved the dependency graph. So If I have function A calling B and C, and C calling B, I can't just time and sum all the occurrences of B for instance, it needs to be contextualised.

Considering this basic example:

```clojure
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
```

This is the result I had in mind when I execute `run`:

```clojure
;;
;; A      = 2 x (5 + .5 + 5) = 21 secs
;; B foo  = 2 x .5 = 1 sec
;; B bar  = 2 x 5 = 10 secs
;; C      = 2 x (.5) = 1 sec
;; D main = 2 x 5 = 10 secs
;; D bar  = 2 x 5 = 10 secs
```

## An excuse to play with macros

Initially I wanted a drop in replacement for `defn` that would be enough for the job. However, it was tricky to find ways to preserve the context. So in the end I went with two macros:

```clojure
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
```

And this is how they're supposed to be used:

```clojure
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

(defn run
  [& _]
  (reset! times [])
  (A (range 10))
```

When you execute `run`, a vector in an atom is populated with each individual run time:

```clojure
  [[[nil
     #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
     :quick-performance.test-case-with-profiling/D]
    1000.18129]
   [[nil
     #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
     :quick-performance.test-case-with-profiling/D]
    1000.3431]
   [[nil
     #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
     :quick-performance.test-case-with-profiling/D]
    1000.347416]
   [[nil
     #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
     :quick-performance.test-case-with-profiling/D]
    1000.125951]
   [[nil
     #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
     :quick-performance.test-case-with-profiling/D]
    1000.193219]
   [[nil
     #uuid "4ac74d7c-719a-4118-8021-8149b99c380f"
     #uuid "8a251e4a-af1b-4096-807c-a34ca29ffa04"
     :quick-performance.test-case-with-profiling/C]
    500.248441]
    ...
```

Then after some post-processing magic, this is the end result:

```clojure
["t/A"
 0.35 ;; total time in minutes
 ["t/D" 47.61] ;; last entry of each vector is the fraction of run time
 ["t/B" 4.77 ["other" 0.0] ["t/C" 4.76]]
 ["t/B" 47.61 ["other" 0.01] ["t/D" 47.61]]]
```

## Why not just flame graphs

Flame graphs give a you lot more information. This approach is easier to setup and there's other advantages for the profiling code to be within the clojure code. Also, the output is a clojure data structure, so you can do whatever you want with it within clojure as well.
