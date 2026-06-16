(ns leonid.config-test
  (:require [clojure.test :refer [deftest is testing]]
            [leonid.config :as config]))

(deftest parse-env-test
  (testing "basic key=value pairs"
    (is (= {"DB_HOST" "localhost" "DB_PORT" "5432"}
           (config/parse-env "DB_HOST=localhost\nDB_PORT=5432"))))

  (testing "blank lines and # comments are ignored"
    (is (= {"A" "1"}
           (config/parse-env "# a comment\n\nA=1\n   \n"))))

  (testing "surrounding quotes are stripped"
    (is (= {"A" "spaced value" "B" "single"}
           (config/parse-env "A=\"spaced value\"\nB='single'"))))

  (testing "values may contain = signs"
    (is (= {"URL" "postgres://x?a=b"}
           (config/parse-env "URL=postgres://x?a=b"))))

  (testing "surrounding whitespace is trimmed from keys and values"
    (is (= {"A" "1"}
           (config/parse-env "  A = 1  ")))))

(deftest db-spec-test
  (testing "defaults when env is empty"
    (is (= {:dbtype "postgresql" :host "localhost" :port 5432
            :dbname "leonid" :user "leonid" :password ""}
           (config/db-spec {}))))

  (testing "values come from the env map"
    (is (= {:dbtype "postgresql" :host "db.internal" :port 6543
            :dbname "app" :user "admin" :password "secret"}
           (config/db-spec {"DB_HOST" "db.internal" "DB_PORT" "6543"
                            "DB_NAME" "app" "DB_USER" "admin"
                            "DB_PASSWORD" "secret"})))))
