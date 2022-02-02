# Check to see we have all the arguments
if($args.Count -lt 2)
{
Write-Host "Use: powershell ./<script name>.ps1 <path> <to> <cc>"
Write-Host "  Please pass the path of the file"
Write-Host "     <to>/<cc>: RECIPIENT MAIL ID SEPARATED BY SEMICOLON WITHOUT ANY SPACE"
Write-Host "     REMEMBER TO PUT <to> and <cc> in SINGLE QUOTES EXAMPLE: powershell ./<SCRIPT NAME>.ps1 <path> '<to>' '<cc>' "
exit
}

$link = "Link"#$args[3]
$to_mail=$args[1]
$cc_mail=$args[2]
$strFileName=$args[0]



# Open excel file and use specific sheet
   # $strFileName="C:\Users\VRI\Desktop\Report.xlsx"
	$objExcel=New-Object -ComObject Excel.Application
    $objExcel.Visible=$false
    $WorkBook=$objExcel.Workbooks.Open($strFileName)
	
	$worksheet = $WorkBook.sheets.Item("Summary")
	
	
	

	$testSuitesExecuted = ""   #
	$environment = ""  			#
	$totalTimeOfExecution = ""		#
	$totalTestCases= 0		 #
	$totaltestCasesPassed= 0			#
	$totaltestCasesFailed = 0
	$totalTestSteps=0					#
	$totalTestStepsPassed=0					#
	$totalTestStepsFailed=0
	$dateOfExecution=""				#
	$buildNumber=""					#
	$currentStatus=""				#
	$style = "
	<style>
    table{border-width: 1px;border-style: solid;border-color: black;border-collapse: collapse;}
    th{border-width: 1px;padding: 3px;border-style: solid;border-color: black;}
    td{border-width: 1px;padding: 3px;border-style: solid;border-color: black;}
	</style>
	"
	
	
	
	# loop for each row of the excel file
	$environment = $worksheet.cells.item(2,2).text
	$buildNumber=  $worksheet.cells.item(2,6).text
	$totalTimeOfExecution = $worksheet.cells.item(2,5).text
	$dateOfExecution = $worksheet.cells.item(2,3).text
	$totalTestCases= $worksheet.cells.item(2,8).value2
	
	
	$currentTestSuite=""
	
    $intSummarySheetRowMax = ($worksheet.UsedRange.Rows).count
	
	Write-Host " $intSummarySheetRowMax"
	
	
	
	for( $intSummarySheetRow=4 ; $intSummarySheetRow -le $intSummarySheetRowMax ;$intSummarySheetRow++)
	{	Write-Host " $intSummarySheetRow"
		$checkTestSuite=$worksheet.cells.item($intSummarySheetRow,2).value2
		$currentTestSuite=$worksheet.cells.item($intSummarySheetRow,2).value2
		
		If(-Not [string]::IsNullOrEmpty($currentTestSuite))
			{
			$tempcurrentTestSuite = "*"+$currentTestSuite+"*"
			if (-Not ($testSuitesExecuted -like $tempcurrentTestSuite) )
			{
				if(($testSuitesExecuted -like ""))
				{
				$testSuitesExecuted=$testSuitesExecuted+$currentTestSuite
				$currentTestSuite=""
				}
				else{
				$testSuitesExecuted=$testSuitesExecuted+";"+$currentTestSuite
				$currentTestSuite=""
				}
			}
		}
		$currentStatus=$worksheet.cells.item($intSummarySheetRow,7).value2
		
		if ($currentStatus -eq "PASS")
		{
		$totaltestCasesPassed++
		}
		else
		{
		$totalTestStepsFailed=$totalTestStepsFailed+$worksheet.cells.item($intSummarySheetRow,10).value2
		$totaltestCasesFailed++
		}
		$totalTestStepsPassed=$totalTestStepsPassed+$worksheet.cells.item($intSummarySheetRow,9).value2
		$totalTestSteps=$totalTestSteps+$worksheet.cells.item($intSummarySheetRow,8).value2
		
		If([string]::IsNullOrEmpty($checkTestSuite)) {
			$intSummarySheetRow = $intSummarySheetRowMax
		}
		
	}
	
	
	
	
	$message="Hi all,<br/>

Please find the Summary of the <b>"+$testSuitesExecuted +"</b>  Test Execution as on "+$dateOfExecution +" on "+$environment +". <br/><br/>

Details are available in attached file for further reference <br/><br/>"
$span=2
	$html_ExecutionSummary="<table >
	<thead>
	<tr>
	<td colspan="+$span+"><b>Execution Summary:$dateOfExecution</b></td>
	
	</tr>
	</thead>
	<tbody>"
	
	$html_ExecutionSummary+="<tr><td>Suites Executed</td><td>"+$testSuitesExecuted+"</td></tr>"+
	"<tr><td>Environment</td><td>"+$environment+"</td></tr>"+
	"<tr><td>Total Time Of Execution</td><td>"+$totalTimeOfExecution+"</td></tr>"+
	"<tr><td>Total Failure</td><td>"+$totaltestCasesFailed+"</td></tr>"+
	"</tbody></table></br>"
	
	
	
	
	$countTable="<br/><table>
	<thead>
	<tr><b>
	<td><b>S.NO.</b></td>
	<td>Component</td>
	<td>Numbers</td>
	</b>
	</tr>
	</thead>
	<tbody>"
	
	$countTable+="<tr>
	<td><b>1</b></td><td>Suites Executed</td><td>"+$testSuitesExecuted+"</td></tr>"+
	"<tr>
	<td><b>2</b></td><td>Total Test Cases</td><td>"+$totalTestCases+"</td></tr>"+
	"<tr>
	<td><b>3</b></td><td>Test Cases Passed</td><td>"+$totaltestCasesPassed+"</td></tr>"+
	"<tr>
	<td><b>4</b></td><td>Test Cases Failed</td><td>"+$totaltestCasesFailed+"</td></tr>"+
	"<tr>
	<td><b>5</b></td><td>Total Test Steps</td><td>"+$totalTestSteps+"</td></tr>"+
	"<tr>
	<td><b>6</b></td><td>Test Steps Passed</td><td>"+$totalTestStepsPassed+"</td></tr>"+
	"<tr>
	<td><b>7</b></td><td>Test Steps Failed</td><td>"+$totalTestStepsFailed+"</td></tr>"+
	"<tr>
	<td><b>8</b></td><td>Build Number</td><td>"+$buildNumber+"</td></tr>"+"</tbody></table><br/><br/><br/><br/>"
	
	
	
	
	
	$message=$message+$html_ExecutionSummary+$countTable
	
	
	if($totaltestCasesFailed -gt 0)
	{
	
		$worksheet = $WorkBook.sheets.Item("Tests")
		
		$message=$message+"<br><b>Details Of Failed Test Steps:</b><br/>"
		
		$failureStepsTable="<table><tr>
		<td><b>Test_Suite</b></td><td><b>TC_ID</b></td><td><b>Comments</b></td>
		</tr>"
		
		# loop for each row of the excel file
		$intRowMax = ($worksheet.UsedRange.Rows).count
		write-host "$intRowMax"
		
		
		for($intRow = 2 ; $intRow -le $intRowMax ; $intRow++)
		{	
		write-host "$intRow"
			
			$stepStatus = $worksheet.cells.item($intRow,5).value2
			
			if ( $stepStatus -eq "FATAL" -or $stepStatus -eq "FAIL")
			{
				$stepFailureRow=""
				$codeName1 = $worksheet.cells.item($intRow,1).value2
				$codeName2 = $worksheet.cells.item($intRow,2).value2
				$codeName4 = $worksheet.cells.item($intRow,4).value2
				$stepFailureRow="<tr><td>"+$codeName1+"</td><td>"+$codeName2+"</td><td>"+$codeName4+"</td></tr>"
				$failureStepsTable=$failureStepsTable+$stepFailureRow
			}
		 
			If([string]::IsNullOrEmpty($stepStatus)) {
				$intRow = $intRowMax
			}
		}
			$failureStepsTable=$failureStepsTable+"</table><br/><br/>"
			$message=$message+$failureStepsTable
		 
	}
	 
	 $replyToMail="mailto:amit.dwivedi@accenture.com?Subject=RE:any"
	 $message=$message+"Please <a href ="+$replyToMail+" >reply to</a> Automation Team for any quires <br> "
	 
	 
	 
    $WorkBook.close($true)
    $objexcel.quit()

	$result = ConvertTo-HTML -head $style -PostContent $message | out-string

#outlook code
 
#Get an Outlook application object
 
$o = New-Object -com Outlook.Application
 
$mail = $o.CreateItem(0)
 
#2 = High importance message
#$mail.importance = 2
 
 $subject=$testSuitesExecuted +"- Test Execution Summary as on "+$dateOfExecution+": Total Test Passed:"+$totaltestCasesPassed+",Test Failed:"+$totaltestCasesFailed +" on "+$environment +" Environment"

$mail.subject = $subject #"$testSuitesExecuted - Test Execution Summary as on $dateOfExecution: Total Test Passed:$totaltestCasesPassed,Test Failed:$totaltestCasesFailed on $environment Environment"
$mail.HTMLBody = $result#"Hi all,<br /><br />Please find the Summary of the Run below and the Run Details attached as an Excel Sheet.<br /><br /><b>Test Summary:</b><br />Test Passed: <b>$pass_test_count</b><br />Test Failed: <b>$fail_test_count<br /><br /></b>Test Steps Passed:<b> $pass_count</b><br />Test Steps Failed:<b> $fail_count</b><br /><br /><b>TEST FAILED AT:</b><br />" + 
 
#separate multiple recipients with a ";"

$mail.To = "$to_mail"
$mail.CC = "$cc_mail"

$mail.Attachments.Add($strFileName);

$mail.Send()
 
# give time to send the email
Start-Sleep 20
 
# quit Outlook
#$o.Quit()
 
#end the script
exit