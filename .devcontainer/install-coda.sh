#!/bin/bash

# Install csh
echo "Installing csh..."
sudo apt-get update && sudo apt-get install -y csh

# Create CODA directory
mkdir -p $HOME/coda

# Download and install CODA 3.10
cd $HOME/coda
echo "Downloading CODA 3.10..."
wget https://coda.jlab.org/drupal/system/files/coda/3.10/coda310_arm-2.tgz -O coda3.10.tgz
tar zxvf coda3.10.tgz -C $HOME/coda
rm coda3.10.tgz

# Download and install JDK 1.8.0_152
echo "Downloading JDK 1.8.0_152..."
wget https://coda.jlab.org/drupal/system/files/coda/3.10/jdk-8u152-linux-x86.tar.gz
tar zxvf jdk-8u152-linux-x86.tar.gz -C $HOME
rm jdk-8u152-linux-x86.tar.gz

# Clone coda_scripts
echo "Cloning coda_scripts..."
cd $HOME/coda
git clone https://github.com/JeffersonLab/coda_scripts.git

# Create CODA environment setup script
echo "Creating CODA environment setup..."
cat > $HOME/coda/setupCODA3.bash << 'EOL'
# Where I installed stuff
export JAVA_HOME=$HOME/jdk1.8.0_152
export CODA=$HOME/coda/3.10_arm
export CODA_CONFIG=$HOME/coda/coda_scripts

# Add CODA binaries to path
export PATH=$CODA/Linux-x86_64/bin:$CODA/common/bin:$CODA_CONFIG:$PATH

# CODA 3 environment defaults
source ${CODA_CONFIG}/coda3.10.setup_bash

# Override some defaults
export SESSION=testsession
export EXPID=testexpid

export COOL_HOME=${CODA}/cool

export REMEX_CMSG_HOST=$(hostname)
export REMEX_CMSG_PASSWORD=${EXPID}

export CODA_COMPONENT_TABLE=${CODA_CONFIG}/config/${EXPID}/coda_component_table.cfg
EOL

# Create directories for CODA configuration
echo "Creating CODA configuration directories..."
mkdir -p $HOME/coda/coda_scripts/config/testexpid

# Create component table configuration
echo "Creating component table configuration..."
cat > $HOME/coda/coda_scripts/config/testexpid/coda_component_table.cfg << 'EOL'
#
#  coda_scripts/config/testexpid/coda_component_table.cfg
#
# hostname   component type    component name
#
$(hostname)  ROC  ROC1
$(hostname)  PEB  PEB1
EOL

# Add CODA environment to .bashrc
echo "Adding CODA environment to .bashrc..."
echo "source \$HOME/coda/setupCODA3.bash" >> $HOME/.bashrc

# Source the environment for the current session
echo "Sourcing CODA environment..."
source $HOME/coda/setupCODA3.bash

# Verify jcedit is in PATH
echo "Verifying jcedit is in PATH..."
which jcedit || echo "jcedit not found in PATH"

echo "CODA installation complete!" 