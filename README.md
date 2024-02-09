# cljs-test-runner [![Clojars Project](https://img.shields.io/clojars/v/olical/cljs-test-runner.svg)](https://clojars.org/olical/cljs-test-runner)

Run all of your [ClojureScript][ClojureScript] tests with one simple command.

Inspired by Cognitect's [test-runner][test-runner] for [Clojure][Clojure], it is
designed to be used in conjunction with the Clojure CLI tool and a `deps.edn`
file.

Under the hood it's building a test runner file, compiling everything and then
executing the compiled tests with [doo][doo] (using
[ingesolvoll's fork][doo-fork]). Discovery of test namespaces is automatic, so
no configuration is required.

## Usage

In simple cases, you'll be able to execute your tests with something as succinct
as the following line.

```bash
$ clojure -Sdeps '{:deps {olical/cljs-test-runner {:mvn/version "3.8.0"}}}' -m cljs-test-runner.main
```

> Note: The generated test code is placed in the directory
> `cljs-test-runner-out` by default (configure with `--out`), you should add
> that to your `.gitignore` file.

It's likely that your tests will require dependencies and configuration that
would become unwieldy in this format. You will need to add the dependency and
`--main` (`-m`) parameter to your `deps.edn` file.

I recommend you put this under an alias such as `test` or `cljs-test` if that's
already taken by your Clojure tests.

```clojure
{:deps {org.clojure/clojure {:mvn/version "1.10.1"}
        org.clojure/clojurescript {:mvn/version "1.10.520"}}
 :aliases {:test {:extra-deps {olical/cljs-test-runner {:mvn/version "3.8.0"}}
                  :main-opts ["-m" "cljs-test-runner.main"]}}}
```

The following will then find, compile and execute your tests through
[node][node].

```bash
$ clojure -Atest

Testing example.partial-test

Testing example.yes-test

Ran 2 tests containing 2 assertions.
0 failures, 0 errors.
```

## Configuration

You can configure the test runner with a few different flags, the most important
one is `--env` (`-x`) which allows you to swap from node to [phantom][phantom]
or chrome-headless if required. I would recommend sticking to node and using
something like [jsdom][jsdom], but this does come down to preference and
technical requirements.

```bash
$ clojure -Atest -x phantom
```

If you need to use `foreign-libs` or any cljs compiler flags that are not
mirrored in cljs-test-runner's flags, you can put them into an EDN file and
point to that file using the `--compile-opts` flag.

You can use `--help` to see the current flags and their default values.

```bash
$ clojure -Atest --help
  -d, --dir DIRNAME            test                  The directory containing your test files
  -n, --namespace SYMBOL                             Symbol indicating a specific namespace to test.
  -r, --namespace-regex REGEX  .*\-test$             Regex for namespaces to test. Only namespaces ending in '-test' are evaluated by default.
  -v, --var SYMBOL                                   Symbol indicating the fully qualified name of a specific test.
  -i, --include SYMBOL                               Run only tests that have this metadata keyword.
  -e, --exclude SYMBOL                               Exclude tests with this metadata keyword.
  -o, --out DIRNAME            cljs-test-runner-out  The output directory for compiled test code
  -x, --env ENV                node                  Run your tests in node, phantom, chrome-headless, lumo or planck.
  -w, --watch DIRNAME                                Directory to watch for changes (alongside the test directory). May be repeated.
  -c, --compile-opts PATH                            EDN file containing opts to be passed to the ClojureScript compiler.
  -D, --doo-opts PATH                                EDN file containing opts to be passed to doo.
  -V, --verbose                                      Flag passed directly to the ClojureScript compiler to enable verbose compiler output.
  -H, --help
```

## Advanced compilation

To use Closure Compiler advanced optimisation levels you will need to create an
EDN file like this:

```edn
{:optimizations :advanced}
```

Now when you run the following, your tests will be executed with advanced
compilation:

```bash
clj -m cljs-test-runner.main -c ./config/advanced-compilation.edn
```

You can also directly inline the EDN using the `-c` flag:

```bash
clj -m cljs-test-runner.main -c '{:optimizations :advanced}'
```

There is a known issue with `:whitespace`, I just haven't invested the time into
working out what it is. For now, stick to `:none`, `:simple` or `:advanced`, the
original issue for optimisation levels breaking things is [#16][#16].

## Bundle target

The new [bundle][bundle] target requires a 2-step process for compilation. One
for building an `index.js` consumable by bundlers like webpack, and a second
step for actually running the bundler. This requires you to specify 2 different
targets.

In essence, this requires you to include the following in your `doo.edn`:

`{:output-to "resources/public/js/main.js"}`

Your CLJS compiler options should be according to the standard guide.

## Gotchas

### Paths

Make sure the directory (or directories!) containing your tests are on your Java
class path. Specify this with a top level `:paths` key in your `deps.edn` file.

### Lumo / Planck

To use Lumo or Planck, add the generated test runner to the `:paths` in your
`deps.edn`:

```edn
:paths ["src" "test" "cljs-test-runner-out/gen"]
```

and set the environment:

    -x lumo

or

    -x planck

## Unlicenced

Find the full [unlicense][unlicense] in the `UNLICENSE` file, but here's a
snippet.

> This is free and unencumbered software released into the public domain.
>
> Anyone is free to copy, modify, publish, use, compile, sell, or distribute
> this software, either in source code form or as a compiled binary, for any
> purpose, commercial or non-commercial, and by any means.

Do what you want. Learn as much as you can. Unlicense more software.

[clojure]: https://clojure.org/
[clojurescript]: https://clojurescript.org/
[test-runner]: https://github.com/cognitect-labs/test-runner
[doo]: https://github.com/bensu/doo
[doo-fork]: https://github.com/ingesolvoll/doo
[node]: https://nodejs.org
[phantom]: http://phantomjs.org/
[jsdom]: https://github.com/jsdom/jsdom
[unlicense]: http://unlicense.org/
[#16]: https://github.com/Olical/cljs-test-runner/issues/16
[bundle]: https://clojurescript.org/guides/webpack
