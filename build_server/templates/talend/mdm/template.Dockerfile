FROM <os_placeholder>:<os_version_placeholder>

MAINTAINER Francisco Duran <franciscogd@gatech.edu>

# Update OS and Install Tools
RUN <update_os_and_install_tools_placeholder>
# Set environment variables for Java version
ENV JDK_VERSION <jdk_version_placeholder>
ENV JDK_UPDATE <jdk_update_placeholder>
ENV JDK_BUILD <jdk_build_placeholder>
ENV JDK_DISTRO_ARCH <jdk_distro_arch_placeholder>
ENV JCE_FOLDER UnlimitedJCEPolicyJDK$JDK_VERSION
ENV JDK_FOLDER jdk1.$JDK_VERSION.0_$JDK_UPDATE
ENV JDK_VERSION_UPDATE $JDK_VERSION'u'$JDK_UPDATE
ENV JDK_VERSION_UPDATE_BUILD $JDK_VERSION_UPDATE'-'$JDK_BUILD
ENV JDK_VERSION_UPDATE_DISTRO_ARCH $JDK_VERSION_UPDATE'-'$JDK_DISTRO_ARCH
ENV JAVA_HOME /opt/java
ENV JRE_SECURITY_FOLDER $JAVA_HOME/jre/lib/security
ENV SSL_TRUSTED_CERTS_FOLDER /opt/ssl/trusted

# Define working directory opt
WORKDIR /opt

#Install Java
RUN wget --no-check-certificate https://github.com/frekele/oracle-java/releases/download/${JDK_VERSION_UPDATE_BUILD}/jdk-${JDK_VERSION_UPDATE_DISTRO_ARCH}.tar.gz \
    && wget --no-check-certificate https://github.com/frekele/oracle-java/releases/download/${JDK_VERSION_UPDATE_BUILD}/jdk-${JDK_VERSION_UPDATE_DISTRO_ARCH}.tar.gz.md5 \
    && echo "$(cat jdk-${JDK_VERSION_UPDATE_DISTRO_ARCH}.tar.gz.md5) *jdk-${JDK_VERSION_UPDATE_DISTRO_ARCH}.tar.gz" | md5sum -c \
    && tar -zvxf jdk-${JDK_VERSION_UPDATE_DISTRO_ARCH}.tar.gz -C /opt \
    && ln -s /opt/${JDK_FOLDER} /opt/java \
    && rm -f jdk-${JDK_VERSION_UPDATE_DISTRO_ARCH}.tar.gz \
    && rm -f jdk-${JDK_VERSION_UPDATE_DISTRO_ARCH}.tar.gz.md5

# Download zip file with java cryptography extension and unzip to jre security folder
RUN wget --no-check-certificate https://github.com/frekele/oracle-java/releases/download/oracle_jce${JDK_VERSION}/jce_policy-${JDK_VERSION}.zip \
    && unzip jce_policy-${JDK_VERSION}.zip \
    && cp ${JCE_FOLDER}/*.jar ${JRE_SECURITY_FOLDER} \
    && rm -f jce_policy-${JDK_VERSION}.zip \
    && rm -rf ${JCE_FOLDER}

# Add executables to path
RUN <add_executables_to_path_place_holder>

# Set environment variables for Tomcat version
ENV CATALINA_HOME /opt/tomcat
ENV PATH $PATH:$JAVA_HOME/bin:$CATALINA_HOME/bin:$CATALINA_HOME/scripts
ENV TOMCAT_MAJOR <tomcat_major_placeholder>
ENV TOMCAT_VERSION <tomcat_version_placeholder>

# Install Tomcat
COPY apache-tomcat-${TOMCAT_VERSION}.tar.gz ./
RUN tar -xvf apache-tomcat-${TOMCAT_VERSION}.tar.gz && \
    rm apache-tomcat*.tar.gz && \
    mv apache-tomcat* ${CATALINA_HOME} && \
    chmod +x ${CATALINA_HOME}/bin/*sh

# Create Tomcat admin user
ADD create_admin_user.sh $CATALINA_HOME/scripts/create_admin_user.sh
ADD tomcat.sh $CATALINA_HOME/scripts/tomcat.sh
RUN chmod +x $CATALINA_HOME/scripts/*.sh
# Create tomcat user
RUN groupadd -r tomcat && \
    useradd -g tomcat -d ${CATALINA_HOME} -s /sbin/nologin  -c "Tomcat user" tomcat && \
    chown -R tomcat:tomcat ${CATALINA_HOME}
# Last step of Tomcat install comes after installing TAC

# Clean all cached files
RUN <clean_cached_files_placeholder>

# Expose Talend Application Ports
EXPOSE 8180

# Define working directory
WORKDIR /talend

# Install <talend_component_placeholder>
ADD <talend_installer_placeholder>.jar /talend

RUN mv /talend/<talend_installer_placeholder>.jar /talend/<talend_component_placeholder>-<talend_version_placeholder>.jar

USER root
# Define Default command
ENTRYPOINT "java -jar /talend/<talend_component_placeholder>-<talend_version_placeholder>.jar" && /bin/bash
