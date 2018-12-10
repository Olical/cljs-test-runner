.PHONY: prepl test

prepl:
	clj -Adev -J-Dclojure.server.jvm="{:port 5005 :accept clojure.core.server/io-prepl}"

test:
	clj -Adev -m cljs-test-runner.main
