ARG BASE_IMAGE="opennms/sentinel-base"
ARG BASE_IMAGE_VERSION="1.0.0"

FROM ${BASE_IMAGE}:${BASE_IMAGE_VERSION}

ARG VERSION=${BASE_IMAGE_VERSION}
ARG BUILD_DATE="1970-01-01T00:00:00+0000"

ARG SENTINEL_PACKAGES="opennms-sentinel"

COPY ./rpms /tmp/rpms

SHELL ["/bin/bash", "-c"]

# Install repositories, system and OpenNMS packages and do some cleanup
RUN if [ "$(ls -1 /tmp/rpms/*.rpm 2>/dev/null | wc -l)" != 0 ]; then yum -y localinstall /tmp/rpms/*.rpm; else yum -y install ${SENTINEL_PACKAGES}; fi && \
    rm -rf /tmp/rpms && \
    yum clean all && \
    rm -rf /var/cache/yum && \
    chown -R ${USER}:${GROUP} /opt/sentinel

USER ${USER}

COPY ./assets/entrypoint.sh /

VOLUME [ "/opt/sentinel/deploy", "/opt/sentinel/etc", "/opt/sentinel/data" ]

LABEL maintainer="The OpenNMS Group" \
      license="AGPLv3" \
      name="Sentinel" \
      version="${VERSION}" \
      build.date="${BUILD_DATE}" \
      vendor="OpenNMS Community"

ENTRYPOINT [ "/entrypoint.sh" ]

STOPSIGNAL SIGTERM

CMD [ "-h" ]

### Runtime information and not relevant at build time

# TODO MVR SENTINEL_LOCATION is not used at the moment
ENV SENTINEL_HOME="/opt/sentinel" \
    SENTINEL_ID="" \
    SENTINEL_LOCATION="SENTINEL" \
    OPENNMS_BROKER_URL="tcp://127.0.0.1:61616" \
    OPENNMS_HTTP_URL="http://127.0.0.1:8980/opennms" \
    OPENNMS_HTTP_USER="minion" \
    OPENNMS_HTTP_PASS="minion" \
    OPENNMS_BROKER_USER="minion" \
    OPENNMS_BROKER_PASS="minion" \
    POSTGRES_HOST="localhost" \
    POSTGRES_PORT="5432" \
    POSTGRES_USER="postgres" \
    POSTGRES_PASSWORD="" \
    POSTGRES_DB="opennms"

##------------------------------------------------------------------------------
## EXPOSED PORTS
##------------------------------------------------------------------------------
## -- Sentinel Karaf Debug 5005/TCP
## -- Sentinel KARAF SSH   8301/TCP

EXPOSE 8301
