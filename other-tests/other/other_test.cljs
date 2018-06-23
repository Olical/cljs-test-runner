(ns other.other-test
  (:require [cljs.test :as t]))

(t/deftest other-should-run
  (t/testing "this should run in the other dir"
    (t/is (= 1 1))))
