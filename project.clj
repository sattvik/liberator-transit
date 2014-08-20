(defproject io.clojure/liberator-transit "0.3.0"
  :description "Library to add Transit encoding support to Liberator"
  :url "https://github.com/sattvik/liberator-transit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.cognitect/transit-clj "0.8.247"]
                 [liberator "0.12.0"]]
  :profiles {:dev {:dependencies [[org.clojure/data.generators "0.1.2"]
                                  [org.clojure/test.check "0.5.9"]
                                  [ring-mock "0.1.5"]
                                  [compojure "1.1.8"]]
                   :plugins [[com.jakemccrary/lein-test-refresh "0.5.0"]
                             [lein-marginalia "0.7.1"]]}})
