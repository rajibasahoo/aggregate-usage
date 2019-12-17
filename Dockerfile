FROM dtrdl.nl.corp.tele2.com:9443/operations/java:8
COPY /aggregate-usage-web/target/aggregate-usage.jar /app/
COPY /aggregate-usage-web/target/classes/appdynamics/ /app/agent/
USER root
RUN chown -R container:container /app/agent
USER container
HEALTHCHECK --start-period=1m --interval=2m --timeout=30s --retries=3 CMD wget -qO- localhost:8080/health &> /dev/null
CMD ["sh", "-c", "exec java -javaagent:/app/agent/javaagent.jar -Dappdynamics.agent.nodeName=$HOSTNAME -Djava.security.egd=file:/dev/./urandom -Xmx500m -XX:MaxRAM=800m -jar /app/aggregate-usage.jar"]
