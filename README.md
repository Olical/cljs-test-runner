# cljs-test-runner [![Clojars Project](https://img.shields.io/clojars/v/olical/cljs-test-runner.svg)](https://clojars.org/olical/cljs-test-runner)

Run all of your [ClojureScript][] tests with one simple command.

Inspired by Cognitect's [test-runner][] for [Clojure][], it is designed to be used in conjunction with the Clojure CLI tool and a `deps.edn` file.

Under the hood it's building a test runner file, compiling everything into the `cljs-test-runner-out` directory and then executing the compiled tests with [doo][]. Discovery of namespaces is automatic, it assumes your tests are located in the `test` directory.

## Usage

In simple cases, you'll be able to execute your tests with something as simple as the following single line.

```
$ clojure -Sdeps '{:deps {olical/cljs-test-runner {:mvn/version "0.1.0-SNAPSHOT"}}}' -m cljs-test-runner.main
```

It's likely that your tests will require dependencies and configuration that would be unwieldy in a single long line of a shell script. You will need to add the dependency and `-m` (`--main`) parameter to your `deps.edn` file.

I recommend you put this under an alias such as `test` or `cljs-test` if that's already taken by your Clojure tests.

```clojure
{:deps {org.clojure/clojure {:mvn/version "1.9.0"}
        org.clojure/clojurescript {:mvn/version "1.10.126"}}
 :aliases {:test {:extra-deps {olical/cljs-test-runner {:mvn/version "0.1.0-SNAPSHOT"}}
                  :main-opts ["-m" "cljs-test-runner.main"]}}}
```

This will (by default) find, compile and execute your tests through [node][]. You can tell it to execute your tests in [phantom][] if you need to, but I'd recommend node and something like [jsdom][] if possible. This generally comes down to personal preference.

```
$ clojure -Atest

Testing example.partial-test

Testing example.yes-test

Ran 2 tests containing 2 assertions.
0 failures, 0 errors.
```

You can use the `--test-env` or `-te` flag to switch between `node` and `phantom`, like so.

```
$ clojure -Atest -te phantom

Testing example.partial-test

Testing example.yes-test

Ran 2 tests containing 2 assertions.
0 failures, 0 errors.
```

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
