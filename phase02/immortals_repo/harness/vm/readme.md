Disable THP on Ubuntu 16.04 LTS (this feature is incompatible with VoltDB, which is the in-memory db Castor uses).

#Step 1: Install sysfsutils

sudo apt install sysfsutils

#Step 2: Add the following to the end of /etc/sysfs.conf

kernel/mm/transparent_hugepage/enabled = never
kernel/mm/transparent_hugepage/defrag = never


#Alternate (using a script)

Use the turnOffTHP.sh script in this folder:

sudo ./turnOffTHP.sh
