# cljs-test-runner changes

## 3.6.0

 * Merged [#31](https://github.com/Olical/cljs-test-runner/pull/31) - Fix optimisation level quirks.

## 3.5.0

 * Merged [#28](https://github.com/Olical/cljs-test-runner/pull/28) - Read inline EDN compiler opts.

## 3.4.0

 * Merged [#25](https://github.com/Olical/cljs-test-runner/pull/25) - Support for [planck](https://github.com/planck-repl/planck).

## 3.3.0

 * Merged [#23](https://github.com/Olical/cljs-test-runner/pull/23) - Avoid crash when specifying a namespace that doesn't exist.
 * Move to Clojure 1.10.0 by default.
 * Clean up some old code left over from a refactor around var filtering.
 * Add a `Makefile` wrapper that can start a prepl server for development.

## 3.2.1

 * Merged [#21](https://github.com/Olical/cljs-test-runner/pull/21) - Filter tests under :advanced.

## 3.2.0

 * Merged [#19](https://github.com/Olical/cljs-test-runner/pull/19) - Adds `chrome-headless` to the envs list.
 * Added some more documentation and caveats about running with `:simple` and `:whitespace` optimisation levels.

## 3.1.0

 * Merged [#17](https://github.com/Olical/cljs-test-runner/pull/17) - Fixes running tests with advanced compilation as mentioned in [#16](https://github.com/Olical/cljs-test-runner/issues/16).

## 3.0.0

 * Merged [#15](https://github.com/Olical/cljs-test-runner/pull/15) - Improves `--dir` so it accumulated but replaces the default if you use it.

## 2.1.0

 * Added `--doo-opts` (`-D`) which is analogous to `--compile-opts`, as recommended by [@johnmn3](https://github.com/johnmn3) in [#9](https://github.com/Olical/cljs-test-runner/issues/9).

## 2.0.0

 * `-e` became `-x` as a shortcut for `--env`.
 * `-h` became `-H`, like the Cognitect test-runner.
 * Added `--namespace` (`-n`) so you can test a single namespace by it's symbol.
 * Added `--namespace-regex` (`-r`) so you can test namespaces matching a regex. This default to any namespace ending in `-test`.
 * Added filtering of tests by symbol or metadata keywords.
 * Added `-V` / `--verbose` for turning the ClojureScript compiler verbose flag on when you want it.
 * Made all options repeatable, so now you can specify `-d test -d other-test-dir` as well as `-v some-test -v some-other-test`.
 * Output the rendered ClojureScript test runner to the output directory, so you only have to git ignore `cljs-test-runner-out`.
 * Added `--compile-opts` thanks to [@kthu](https://github.com/kthu) in [#7](https://github.com/Olical/cljs-test-runner/pull/7).
 * Any argument that takes a keyword will now parse both `:foo` and `foo` as the same.

## 1.0.0

 * `--watch` support thanks to [@eval](https://github.com/eval) in [#2](https://github.com/Olical/cljs-test-runner/pull/2).

## 0.1.1

 * Print errors that originate from the ClojureScript compiler or doo. Things like missing namespaces were failing silently.

## 0.1.0

 * Initial release.
