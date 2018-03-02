(ns example.foo-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest some-test
  (testing "oh hai"
    (is (= 1 1))))
