#!/usr/bin/env bash

<<<<<<< HEAD
set -e


#Usage: setting up our networking resources such as Virtual Private Cloud (VPC), Internet Gateway,
# Route Table and Routes using AWS Cloud Formation

STACK_NAME=$1

#Create Stack:

aws cloudformation create-stack --stack-name $STACK_NAME --template-body file://csye6225-cf-networking.json

#Check Stack Status
STACK_STATUS=`aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[][ [StackStatus ] ][]" --output text`

#Wait until stack completely created
echo "Please wait..."

while [ $STACK_STATUS != "CREATE_COMPLETE" ]
do

	STACK_STATUS=`aws cloudformation describe-stacks --stack-name $STACK_NAME --query "Stacks[][ [StackStatus ] ][]" --output text`

done

#Find vpc Id
vpcId=`aws ec2 describe-vpcs --filter "Name=tag:Name,Values=${STACK_NAME}" --query 'Vpcs[*].{id:VpcId}' --output text`
#Rename vpc
aws ec2 create-tags --resources $vpcId --tags Key=Name,Value=$STACK_NAME-csye6225-vpc

#Find Internet Gateway
gatewayId=`aws ec2 describe-internet-gateways --filter "Name=tag:Name,Values=${STACK_NAME}" --query 'InternetGateways[*].{id:InternetGatewayId}' --output text`
#Rename Internet Gateway
aws ec2 create-tags --resources $gatewayId --tags Key=Name,Value=$STACK_NAME-csye6225-InternetGateway

#Find Route Table
routeTableId=`aws ec2 describe-route-tables --filter "Name=tag:Name,Values=${STACK_NAME}" --query 'RouteTables[*].{id:RouteTableId}' --output text`
#Rename Route Table
aws ec2 create-tags --resources $routeTableId --tags Key=Name,Value=$STACK_NAME-csye6225-public-route-table

#Job Done!
echo "Job Done!"
=======
#Arguments: STACK_NAME

STACK_NAME=$1
#Create VPC and get its Id
vpcId=`aws ec2 create-vpc --cidr-block 10.0.0.0/16 --query 'Vpc.VpcId' --output text`
#Tag vpc
aws ec2 create-tags --resources $vpcId --tags Key=Name,Value=$STACK_NAME-csye6225-vpc
echo "Vpc created-> Vpc Id:  "$vpcId

#Create subnets
subnetId=`aws ec2 create-subnet --vpc-id $vpcId --cidr-block 10.0.0.0/24 --query 'Subnet.SubnetId' --output text`
#Tag subnet
aws ec2 create-tags --resources $subnetId --tags Key=Name,Value=$STACK_NAME-csye6225-subnet

subnetId2=`aws ec2 create-subnet --vpc-id $vpcId --cidr-block 10.0.1.0/24 --query 'Subnet.SubnetId' --output text`
aws ec2 create-tags --resources $subnetId2 --tags Key=Name,Value=$STACK_NAME-csye6225-subnet2

subnetId3=`aws ec2 create-subnet --vpc-id $vpcId --cidr-block 10.0.2.0/24 --query 'Subnet.SubnetId' --output text`
aws ec2 create-tags --resources $subnetId3 --tags Key=Name,Value=$STACK_NAME-csye6225-subnet3
#aws ec2 create-subnet --vpc-id $vipId --cidr-block 10.0.2.0/24

#Create Internet Gateway
gatewayId=`aws ec2 create-internet-gateway --query 'InternetGateway.InternetGatewayId' --output text`
#Tag Internet Gateway
aws ec2 create-tags --resources $gatewayId --tags Key=Name,Value=$STACK_NAME-csye6225-InternetGateway
echo "Internet gateway created-> gateway Id: "$gatewayId

#Attach Internet Gateway to Vpc
aws ec2 attach-internet-gateway --internet-gateway-id $gatewayId --vpc-id $vpcId
echo "Attached Internet gateway: "$gatewayId" to Vpc: "$vpcId

#Create Route Table
routeTableId=`aws ec2 create-route-table --vpc-id $vpcId --query 'RouteTable.RouteTableId' --output text`
#Tag Route Table
aws ec2 create-tags --resources $routeTableId --tags Key=Name,Value=$STACK_NAME-csye6225-rt
echo "Route table created -> route table Id: "$routeTableId

#Create Route
aws ec2 create-route --route-table-id $routeTableId --destination-cidr-block 0.0.0.0/0 --gateway-id $gatewayId
echo "Route created: in "$routeTableId" target to "$gatewayId

#Attach route tables to subnets
aws ec2 associate-route-table --subnet-id $subnetId --route-table-id $routeTableId
aws ec2 associate-route-table --subnet-id $subnetId2 --route-table-id $routeTableId
aws ec2 associate-route-table --subnet-id $subnetId3 --route-table-id $routeTableId

#Job Done
echo "Job Done!"

>>>>>>> 9e133baba1a4391db760c27afb9ebc087ca3ad59
