(ns example.yes-test
  (:require [cljs.test :as t]))

(t/deftest should-run
  (t/testing "this should run"
    (t/is (= 1 1))))

(t/deftest ^:integration maybe-run
  (t/testing "this may run"
    (t/is (= 2 2))))
