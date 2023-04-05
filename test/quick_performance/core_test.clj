(ns quick-performance.core-test
  (:require [clojure.test :refer :all]
            [quick-performance.core :as sut]))

(def all-times
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
   [[nil
     #uuid "4ac74d7c-719a-4118-8021-8149b99c380f"
     :quick-performance.test-case-with-profiling/B]
    500.966261]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
     :quick-performance.test-case-with-profiling/D]
    1000.104248]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
     :quick-performance.test-case-with-profiling/D]
    1000.137593]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
     :quick-performance.test-case-with-profiling/D]
    1000.108928]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
     :quick-performance.test-case-with-profiling/D]
    1000.122232]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
     :quick-performance.test-case-with-profiling/D]
    1000.082911]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     :quick-performance.test-case-with-profiling/B]
    5001.370074]
   [[nil
     #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
     :quick-performance.test-case-with-profiling/D]
    1000.169799]
   [[nil
     #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
     :quick-performance.test-case-with-profiling/D]
    1000.128479]
   [[nil
     #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
     :quick-performance.test-case-with-profiling/D]
    1000.112643]
   [[nil
     #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
     :quick-performance.test-case-with-profiling/D]
    1000.078827]
   [[nil
     #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
     :quick-performance.test-case-with-profiling/D]
    1000.193693]
   [[nil
     #uuid "4ac74d7c-719a-4118-8021-8149b99c380f"
     #uuid "8a251e4a-af1b-4096-807c-a34ca29ffa04"
     :quick-performance.test-case-with-profiling/C]
    500.191375]
   [[nil
     #uuid "4ac74d7c-719a-4118-8021-8149b99c380f"
     :quick-performance.test-case-with-profiling/B]
    500.256215]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
     :quick-performance.test-case-with-profiling/D]
    1000.211811]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
     :quick-performance.test-case-with-profiling/D]
    1000.15745]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
     :quick-performance.test-case-with-profiling/D]
    1000.089126]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
     :quick-performance.test-case-with-profiling/D]
    1000.12856]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
     :quick-performance.test-case-with-profiling/D]
    1000.179644]
   [[nil
     #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
     :quick-performance.test-case-with-profiling/B]
    5001.079178]
   [[nil :quick-performance.test-case-with-profiling/A] 21007.99577]])

(deftest sum-times-and-group-by-ref-ordered-test
  (let [[A D-main B-foo B-bar C D-bar]
        (sut/sum-times-and-group-by-ref-ordered all-times)]
    (is (= 10 (-> D-main last (/ 1000) int)))
    (is (= 1 (-> C last (/ 1000) int)))
    (is (= 1 (-> B-foo last (/ 1000) int)))
    (is (= 10 (-> D-bar last (/ 1000) int)))
    (is (= 10 (-> B-bar last (/ 1000) int)))
    (is (= 21 (-> A last (/ 1000) int)))))

(deftest skim-top-level-test
  (is
   (= [:quick-performance.test-case-with-profiling/D  ;; D-main
       :quick-performance.test-case-with-profiling/B  ;;foo
       :quick-performance.test-case-with-profiling/B] ;;bar
      (->> all-times
           sut/sum-times-and-group-by-ref-ordered
           rest
           sut/skim-top-level
           (map first)
           (map last)))))

(deftest get-all-children-test
  (is (=
       [[nil
         #uuid "15a1f569-9f9f-4a4b-9502-b2655de327b6"
         :quick-performance.test-case-with-profiling/D]
        [nil
         #uuid "4ac74d7c-719a-4118-8021-8149b99c380f"
         :quick-performance.test-case-with-profiling/B]
        [nil
         #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
         :quick-performance.test-case-with-profiling/B]
        [nil
         #uuid "4ac74d7c-719a-4118-8021-8149b99c380f"
         #uuid "8a251e4a-af1b-4096-807c-a34ca29ffa04"
         :quick-performance.test-case-with-profiling/C]
        [nil
         #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
         #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
         :quick-performance.test-case-with-profiling/D]]
       (map first
            (sut/get-all-children
             [nil :quick-performance.test-case-with-profiling/A]
             (sut/sum-times-and-group-by-ref-ordered all-times)))))

  (is (=
       [[nil
         #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
         #uuid "5e2bed7c-4d1a-4351-8e6a-884705190f21"
         :quick-performance.test-case-with-profiling/D]]
       (map first
            (sut/get-all-children
             [nil
              #uuid "6a057e45-8da6-49c1-8b4d-322d9a83fac1"
              :quick-performance.test-case-with-profiling/B]
             (sut/sum-times-and-group-by-ref-ordered all-times))))))


(reset! times all-times)

(sut/post-process)
