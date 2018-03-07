.PHONY: test deploy

test-node:
	clojure -Atest

test-phantom:
	clojure -Atest --env phantom

deploy:
	clj -Spom
	mvn deploy
