(ns example.nope-test
  (:require [clojure.test :as t]))

(t/deftest should-not-run
  (t/testing "this should not run"
    (t/is (= 1 2))))
