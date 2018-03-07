# cljs-test-runner [![Clojars Project](https://img.shields.io/clojars/v/olical/cljs-test-runner.svg)](https://clojars.org/olical/cljs-test-runner)

Run all of your [ClojureScript][] tests with one simple command.

Inspired by Cognitect's [test-runner][] for [Clojure][], it is designed to be used in conjunction with Clojure 1.9's CLI tool and a `deps.edn` file.

Under the hood, it's building a test runner file for you, compiling everything into the `cljs-test-runner-out` directory and then executing the compiled tests with [doo][]. Discovery of namespaces is automatic, it assumes your tests are located in the `test` directory.

## Usage

You're going to want to add the dependency and a call to the `cljs-test-runner.main` namespace to your `deps.edn` file. I recommend you put this under the `test` alias, like so.

```clojure
{:deps {org.clojure/clojure {:mvn/version "1.9.0"}
        org.clojure/clojurescript {:mvn/version "1.10.126"}}
 :aliases {:test {:extra-paths ["test"]
                  :extra-deps {olical/cljs-test-runner {:mvn/version "1.0.0"}}
                  :main-opts ["-m" "cljs-test-runner.main"]}}}
```

This will (by default) find, compile and execute your tests through [node][]. You can tell it to execute your tests in [phantom][] if you need to, but I'd recommend node and something like [jsdom][] if possible.

```
# Execute all of your tests in node.
clojure -Atest

Testing example.partial-test

Testing example.yes-test

Ran 2 tests containing 2 assertions.
0 failures, 0 errors.

# Execute all of your tests in phantom.
clojure -Atest --env phantom
```

The `--env` flag simply defaults to `node`, you can specify it within your `main-opts` of your alias if you want to.

## Unlicenced

Find the full [unlicense][] in the `UNLICENSE` file, but here's a snippet.

>This is free and unencumbered software released into the public domain.
>
>Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and by any means.

Do what you want. Learn as much as you can. Unlicense more software.

[clojure]: https://clojure.org/
[clojurescript]: https://clojurescript.org/
[test-runner]: https://github.com/cognitect-labs/test-runner
[doo]: https://github.com/bensu/doo
[node]: https://nodejs.org
[phantom]: http://phantomjs.org/
[jsdom]: https://github.com/jsdom/jsdom
[unlicense]: http://unlicense.org/
