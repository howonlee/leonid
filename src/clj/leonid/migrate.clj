(ns leonid.migrate
  "Migratus entry point. Run via the :migrate alias, e.g.

     clojure -M:migrate create add-users     ; scaffold up/down SQL files
     clojure -M:migrate migrate               ; apply all pending migrations
     clojure -M:migrate rollback              ; roll back the last migration
     clojure -M:migrate pending               ; list unapplied migrations
     clojure -M:migrate up 20260616120000     ; apply a specific migration
     clojure -M:migrate down 20260616120000   ; roll back a specific migration"
  (:require [leonid.config :as config]
            [migratus.core :as migratus]))

(defn migratus-config
  "Build the Migratus config map from the merged environment."
  ([] (migratus-config (config/load-env)))
  ([env]
   {:store         :database
    :migration-dir "migrations"
    :db            (config/db-spec env)}))

(defn -main [& args]
  (let [cfg          (migratus-config)
        [cmd & more] args]
    (case cmd
      "migrate"  (migratus/migrate cfg)
      "rollback" (migratus/rollback cfg)
      "pending"  (println "Pending:" (migratus/pending-list cfg))
      "create"   (migratus/create cfg (first more))
      "up"       (apply migratus/up cfg (map parse-long more))
      "down"     (apply migratus/down cfg (map parse-long more))
      (println "Usage: migrate | rollback | pending | create <name> | up <id> | down <id>"))
    (shutdown-agents)))
