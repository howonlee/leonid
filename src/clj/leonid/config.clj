(ns leonid.config
  "Configuration loaded from a .env file (gitignored) merged with the process
   environment. Real environment variables win over .env, matching dotenv
   convention. The parsing functions are pure so they can be unit-tested."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- unquote-value
  "Strip a single matching pair of surrounding single/double quotes, if present."
  [v]
  (if (and (>= (count v) 2)
           (or (and (str/starts-with? v "\"") (str/ends-with? v "\""))
               (and (str/starts-with? v "'")  (str/ends-with? v "'"))))
    (subs v 1 (dec (count v)))
    v))

(defn parse-env
  "Parse the contents of a .env file into a {\"KEY\" \"value\"} map.
   Ignores blank lines and # comments. Pure."
  [content]
  (->> (str/split-lines content)
       (map str/trim)
       (remove #(or (str/blank? %) (str/starts-with? % "#")))
       (keep (fn [line]
               (when-let [idx (str/index-of line "=")]
                 [(str/trim (subs line 0 idx))
                  (unquote-value (str/trim (subs line (inc idx))))])))
       (into {})))

(defn load-env
  "Return the merged environment as a {\"KEY\" \"value\"} map: values from the
   .env file (if it exists) overridden by any real environment variables."
  ([] (load-env ".env"))
  ([path]
   (let [file      (io/file path)
         from-file (when (.exists file) (parse-env (slurp file)))]
     (merge from-file (into {} (System/getenv))))))

(defn db-spec
  "Build a next.jdbc / Migratus db-spec from an env map (see `load-env`). Pure."
  [env]
  {:dbtype   "postgresql"
   :host     (get env "DB_HOST" "localhost")
   :port     (parse-long (get env "DB_PORT" "5432"))
   :dbname   (get env "DB_NAME" "leonid")
   :user     (get env "DB_USER" "leonid")
   :password (get env "DB_PASSWORD" "")})

(defn pool-max-size
  "Connection-pool max size from the env map. Pure."
  [env]
  (parse-long (get env "DB_POOL_MAX_SIZE" "10")))
