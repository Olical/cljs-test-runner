# cljs-test-runner changes

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
