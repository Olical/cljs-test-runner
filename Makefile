.PHONY: nrepl-server test

test:
	clojure -A:dev:test

nrepl-server:
	clojure -A:dev:nrepl-server
