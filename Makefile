.PHONY: nrepl test

test:
	clj -Atest

nrepl:
	clj -Anrepl
