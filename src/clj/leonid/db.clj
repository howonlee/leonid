(ns leonid.db
  "Database access: a HikariCP connection pool plus thin query helpers that take
   HoneySQL data and return plain (unqualified, kebab-cased) Clojure maps."
  (:require [honey.sql :as sql]
            [leonid.config :as config]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [next.jdbc.result-set :as rs])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defonce ^:private datasource (atom nil))

(def ^:private opts
  "Return unqualified, kebab-cased maps (e.g. :user-id rather than :users/user_id)."
  {:builder-fn rs/as-unqualified-kebab-maps})

(defn start!
  "Create and cache the connection pool. Idempotent: returns the existing pool
   if already started. Reads config from the merged environment by default."
  ([] (start! (config/load-env)))
  ([env]
   (or @datasource
       (reset! datasource
               (connection/->pool
                HikariDataSource
                (assoc (config/db-spec env)
                       :maximumPoolSize (config/pool-max-size env)))))))

(defn stop!
  "Close and clear the connection pool."
  []
  (when-let [^HikariDataSource ds @datasource]
    (.close ds)
    (reset! datasource nil)))

(defn ds
  "The active datasource, starting the pool lazily if needed."
  []
  (or @datasource (start!)))

(defn query
  "Run a HoneySQL map as a SELECT, returning a vector of maps."
  ([sqlmap] (query (ds) sqlmap))
  ([connectable sqlmap]
   (jdbc/execute! connectable (sql/format sqlmap) opts)))

(defn query-one
  "Run a HoneySQL map and return the first row (or nil)."
  ([sqlmap] (query-one (ds) sqlmap))
  ([connectable sqlmap]
   (jdbc/execute-one! connectable (sql/format sqlmap) opts)))

(defn execute!
  "Run a HoneySQL INSERT/UPDATE/DELETE. With a :returning clause the affected
   rows come back as maps; otherwise an update count is returned."
  ([sqlmap] (execute! (ds) sqlmap))
  ([connectable sqlmap]
   (jdbc/execute! connectable (sql/format sqlmap) opts)))

(defn with-transaction*
  "Run (f tx) inside a transaction. Prefer the `with-transaction` macro."
  [f]
  (jdbc/with-transaction [tx (ds)]
    (f tx)))

(defmacro with-transaction
  "Bind `tx-sym` to a transactional connection for the body. Pass `tx-sym` as the
   connectable to `query`/`execute!` inside the body."
  [[tx-sym] & body]
  `(with-transaction* (fn [~tx-sym] ~@body)))
