(ns example.ignore-me)

(throw (js/Error. "Should not run or load, it does not end with -test."))
