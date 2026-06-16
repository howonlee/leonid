# AGENT.md

Guidance for agents (and humans) working in this repo.

leonid is a full-stack Clojure app for economic planning. It is composed of a
Ring/Jetty/Reitit backend (`src/clj`) and a shadow-cljs/Reagent frontend
(`src/cljs`). See `README.md` for setup and the dev workflow.

## Conventions

1. **Prefer pure functions whenever possible.** Push side effects (I/O, state
   mutation, HTTP, DOM) to the edges and keep the core logic as plain functions
   of their inputs. Pure functions are easier to test, reason about, and poke at
   in the REPL.

2. **Leave `(comment ...)` blocks at the end of files and sections.** In
   addition to formal tests with `clojure.test` and `test.check`, add rich
   comment blocks (Rich comments) holding example calls, sample data, and
   exploratory expressions. These let you evaluate forms inline against a live
   namespace and document how a piece of code is meant to be exercised.

   ```clojure
   (comment
     ;; eval these forms in the REPL against this namespace
     (my-fn {:x 1})
     (require '[clojure.test.check.generators :as gen])
     ,)
   ```

3. **Keep implementations nREPL-friendly.** Write code so it's easy to connect
   an nREPL and work interactively: prefer small, individually-evaluable
   functions over deeply nested or monolithic forms; avoid top-level side
   effects that fire on namespace load; make state (servers, atoms, connections)
   reachable and restartable from the REPL so you can redefine and re-test
   pieces without restarting the process.
