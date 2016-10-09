(defproject io.clojure/liberator-transit "0.3.1"
  :description "Library to add Transit encoding support to Liberator"
  :url "https://github.com/sattvik/liberator-transit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [com.cognitect/transit-clj "0.8.290"]
                 [liberator "0.14.1"]]
  :profiles {:dev {:dependencies [[com.gfredericks/test.chuck "0.2.7"]
                                  [compojure "1.5.1"]
                                  [org.clojure/test.check "0.9.0"]
                                  [ring-mock "0.1.5"]]}})
