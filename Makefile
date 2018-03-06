.PHONY: test-node test-browser

test-node:
	clojure -Atest -re node

test-browser:
	clojure -Atest -re browser
