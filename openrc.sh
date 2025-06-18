#!/usr/bin/env bash
# To use an OpenStack cloud you need to authenticate against the Identity
# service named keystone, which returns a **Token** and **Service Catalog**.
# The catalog contains the endpoints for all services the user/tenant has
# access to - such as Compute, Image Service, Identity, Object Storage, Block
# Storage, and Networking (code-named nova, glance, keystone, swift,
# cinder, and neutron).
#
# *NOTE*: Using the 3 *Identity API* does not necessarily mean any other
# OpenStack API is version 3. For example, your cloud provider may implement
# Image API v1.1, Block Storage API v2, and Compute API v2.0. OS_AUTH_URL is
# only for the Identity API served through keystone.
export OS_AUTH_URL=http://control.cercatrova.uk:5000/v3/

# With Keystone you pass the keystone password.
echo "Please enter your OpenStack Credential ID as OS_APPLICATION_CREDENTIAL_ID: "
read -sr OS_APPLICATION_CREDENTIAL_ID
export OS_APPLICATION_CREDENTIAL_ID=$OS_APPLICATION_CREDENTIAL_ID
echo "Please enter your OpenStack Credential Secret as OS_APPLICATION_CREDENTIAL_SECRET: "
read -sr OS_APPLICATION_CREDENTIAL_SECRET
export OS_APPLICATION_CREDENTIAL_SECRET=$OS_APPLICATION_CREDENTIAL_SECRET

# Don't leave a blank variable, unset it if it was empty
if [ -z "$OS_REGION_NAME" ]; then unset OS_REGION_NAME; fi
export OS_INTERFACE=public
export OS_IDENTITY_API_VERSION=3
export OS_AUTH_TYPE=v3applicationcredential
# If your configuration has multiple regions, we set that information here.
# OS_REGION_NAME is optional and only valid in certain environments.
export OS_REGION_NAME=HoChiMinh

# If OS_AUTH_URL use private SSL, Please add CACERT file path 
# export OS_CACERT={crtPath}