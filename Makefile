.PHONY: nrepl-server test

test:
	clojure -Atest

nrepl-server:
	clojure -Anrepl-server
