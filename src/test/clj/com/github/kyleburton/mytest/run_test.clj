
 (ns com.github.kyleburton.mytest.run-test
  (:use clojure.contrib.test.junit
        clojure.contrib.test-is)
  (:gen-class))

(def *all-tests*
  '())

(defn -main [& args]
  (apply run-tests *all-tests*))
