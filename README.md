# Car-Text-Image-Recognition-AWS

Step 1: 
Create 2 EC2 instances EC2_A and EC2_B using AWS management console.

goto Services -> "EC2"
goto on Instances in the drop down Instances Section 
Click on Launch Instances 
Select the Amazon Linux 2 AMI (HVM) - Kernel 5.10, SSD Volume Type - ami-0cff7528ff583bf9a (64-bit x86) / ami-00bf5f1c358708486 (64-bit Arm)  
Select the t2.micro type
Click Next: Configure Instance Details
generate key for EC2_A and use same key from drop down while creating EC2_B
Click Next: Configure Security Group
select option - SSH, HTTP, HTTPS
Under 'Source' drop down for each rule, select 'My IP'
Click Review and Launch
Click on Launch 
	
Step 2 : 

Download Eclipse on local machine and configure maven in it.
Configure EC2 instances in the Eclipse by copy and pasting keys from AWS Details.
Create maven project.
Build jar file using Eclipse.

Step 3:
Download putty. Login to EC2 instances using putty.
Download WinSCP to copy jar file to the EC2 instances.

Step 4:
update java version on both EC2 instances using below commands:
sudo amazon-linux-extras install java-openjdk11
sudo yum install java-1.8.0-openjdk
alternatives --config java

Step 5:
Set credentials from environment variables using below commands
$ export AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
$ export AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
$ export AWS_SESSION_TOKEN=AQoDYXdzEJr...<remainder of security token>

Step 6:
Run both the jar files on the respective EC2 instances using below command
$ java -jar jar_name

  
