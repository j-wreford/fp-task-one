(defproject server "0.1.0-SNAPSHOT"
  :description "Functional Programming Assignment 2"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/data.json "1.0.0"]
                 [clojurewerkz/support "1.5.0"]
                 [org.mongodb/mongodb-driver "3.12.2"]
                 [org.mongodb/mongodb-driver-sync "4.0.0"]
                 [com.novemberain/monger "3.1.0"]
                 [ring "1.8.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [compojure "1.6.1"]
                 [cheshire "5.10.0"]

                 ;; client
                 [org.clojure/data.priority-map "1.0.0"]]
  :ring {:handler server.core/-main}
  :repl-options {:init-ns server.core}
  :main server.core)