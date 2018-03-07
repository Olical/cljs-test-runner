.PHONY: test deploy

test:
	clojure -Atest

deploy:
	clj -Spom
	mvn deploy
