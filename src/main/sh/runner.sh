CLASSPATH=@target.directory@/classes:@maven.test.classpath@

    #-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8886 \
    #-Xdebug \
java -server \
    -Dcom.github.kyleburton.project-dir=@basedir@ \
    -cp "$CLASSPATH" \
    com.github.psantacl.ga_sandbox.einstein_main \
    "@target.directory@/classes/swank_runner.clj" \
    "$@"
