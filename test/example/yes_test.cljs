(ns example.yes-test
  (:require [cljs.test :as t]))

(t/deftest should-run
  (t/testing "this should run"
    (t/is (= 1 1))))
