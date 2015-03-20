# -*- mode: ruby -*-
# vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  #config.vm.box = "hashicorp/precise64"
  config.vm.box = "ubuntu/trusty64"
  config.ssh.forward_agent = TRUE
  
  config.vm.provider "virtualbox" do |vb|
    vb.memory = 4096
    vb.cpus = 2
  end

  config.vm.provision "shell", path: "provision.sh"
  
  # /vagrant folder will be mounted as VirtualBox shared folder
  # which seems to have problems with hardlinks (and possible symlinks).
  # So we mount it as NFS instead (this also brings ~10 times faster access)
  config.vm.synced_folder ".", "/vagrant", type: "nfs"
  config.vm.network :private_network, ip: "10.11.12.13"

end
