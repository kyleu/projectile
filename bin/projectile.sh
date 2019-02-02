#!/bin/bash
set -e

working_dir=${pwd}
script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ ! -f ${script_dir}/projectile.jar ]; then
  echo "Downloading latest Projectile release..."
  cd $script_dir
  curl -s https://api.github.com/repos/KyleU/projectile/releases/latest | grep "browser_download_url.*jar" | cut -d : -f 2,3 | tr -d \" | wget -qi -
  cd $working_dir
  echo "Finished download"
fi

# Run jar using original working directory
java -jar "${script_dir}/projectile.jar" $@
