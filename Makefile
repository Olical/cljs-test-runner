.PHONY: nrepl-server test

test:
	clj -Atest

nrepl-server:
	clj -Anrepl-server
