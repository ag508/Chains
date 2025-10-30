package com.chain.messaging

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Generates comprehensive HTML test reports for the Chain messaging app test suite
 */
class TestReportGenerator {

    fun generateHTMLReport(testResults: ComprehensiveTestResults): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        val totalTime = testResults.endTime - testResults.startTime
        
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chain Messaging App - Comprehensive Test Report</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 40px; padding-bottom: 20px; border-bottom: 3px solid #007acc; }
        .header h1 { color: #007acc; margin: 0; font-size: 2.5em; }
        .header .subtitle { color: #666; font-size: 1.2em; margin-top: 10px; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 40px; }
        .summary-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 8px; text-align: center; }
        .summary-card.success { background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); }
        .summary-card.warning { background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%); }
        .summary-card.error { background: linear-gradient(135deg, #f44336 0%, #d32f2f 100%); }
        .summary-card h3 { margin: 0 0 10px 0; font-size: 1.1em; opacity: 0.9; }
        .summary-card .value { font-size: 2.5em; font-weight: bold; margin: 0; }
        .test-suite { margin-bottom: 30px; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; }
        .suite-header { background: #f8f9fa; padding: 15px 20px; border-bottom: 1px solid #ddd; display: flex; justify-content: space-between; align-items: center; }
        .suite-header h3 { margin: 0; color: #333; }
        .suite-status { padding: 5px 15px; border-radius: 20px; color: white; font-weight: bold; font-size: 0.9em; }
        .suite-status.passed { background: #4CAF50; }
        .suite-status.failed { background: #f44336; }
        .suite-content { padding: 20px; }
        .test-details { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px; margin-bottom: 20px; }
        .detail-item { background: #f8f9fa; padding: 15px; border-radius: 5px; border-left: 4px solid #007acc; }
        .detail-item strong { color: #333; }
        .requirements { margin-top: 40px; }
        .requirements h2 { color: #007acc; border-bottom: 2px solid #007acc; padding-bottom: 10px; }
        .requirement { display: flex; align-items: center; padding: 10px; margin: 5px 0; border-radius: 5px; background: #f8f9fa; }
        .requirement.passed { border-left: 4px solid #4CAF50; }
        .requirement.failed { border-left: 4px solid #f44336; }
        .requirement-icon { margin-right: 15px; font-size: 1.2em; font-weight: bold; }
        .requirement-icon.passed { color: #4CAF50; }
        .requirement-icon.failed { color: #f44336; }
        .errors { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin-top: 15px; }
        .errors h4 { color: #856404; margin: 0 0 10px 0; }
        .error-item { background: white; padding: 10px; margin: 5px 0; border-radius: 3px; border-left: 3px solid #f44336; font-family: monospace; font-size: 0.9em; }
        .footer { text-align: center; margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; }
        .progress-bar { width: 100%; height: 20px; background: #e0e0e0; border-radius: 10px; overflow: hidden; margin: 10px 0; }
        .progress-fill { height: 100%; background: linear-gradient(90deg, #4CAF50, #45a049); transition: width 0.3s ease; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Chain Messaging App</h1>
            <div class="subtitle">Comprehensive Test Suite Report</div>
            <div style="margin-top: 15px; color: #888;">Generated on $timestamp</div>
        </div>

        <div class="summary">
            <div class="summary-card ${if (testResults.overallSuccess) "success" else "error"}">
                <h3>Overall Status</h3>
                <p class="value">${if (testResults.overallSuccess) "PASS" else "FAIL"}</p>
            </div>
            <div class="summary-card">
                <h3>Test Suites</h3>
                <p class="value">${testResults.suiteResults.size}</p>
            </div>
            <div class="summary-card success">
                <h3>Passed</h3>
                <p class="value">${testResults.totalPassed}</p>
            </div>
            <div class="summary-card ${if (testResults.totalFailed > 0) "error" else "success"}">
                <h3>Failed</h3>
                <p class="value">${testResults.totalFailed}</p>
            </div>
            <div class="summary-card">
                <h3>Success Rate</h3>
                <p class="value">${String.format("%.1f", testResults.successRate)}%</p>
            </div>
            <div class="summary-card">
                <h3>Execution Time</h3>
                <p class="value">${String.format("%.1f", totalTime / 1000.0)}s</p>
            </div>
        </div>

        <div class="progress-bar">
            <div class="progress-fill" style="width: ${testResults.successRate}%"></div>
        </div>

        ${generateSuiteReports(testResults.suiteResults)}

        ${generateRequirementsReport(testResults.requirementValidation)}

        <div class="footer">
            <p>Chain Messaging App Test Suite - Validating all requirements for decentralized messaging platform</p>
            <p>Report generated at $timestamp | Total execution time: ${String.format("%.2f", totalTime / 1000.0)} seconds</p>
        </div>
    </div>
</body>
</html>
        """.trimIndent()
    }

    private fun generateSuiteReports(suiteResults: List<TestSuiteResult>): String {
        return suiteResults.joinToString("\n") { suite ->
            val status = if (suite.failed == 0) "passed" else "failed"
            val suiteSuccessRate = if ((suite.passed + suite.failed) > 0) {
                (suite.passed.toDouble() / (suite.passed + suite.failed)) * 100
            } else 0.0

            """
            <div class="test-suite">
                <div class="suite-header">
                    <h3>${suite.name}</h3>
                    <span class="suite-status $status">${status.uppercase()}</span>
                </div>
                <div class="suite-content">
                    <div class="test-details">
                        <div class="detail-item">
                            <strong>Success Rate:</strong> ${String.format("%.1f", suiteSuccessRate)}%
                        </div>
                        <div class="detail-item">
                            <strong>Execution Time:</strong> ${suite.executionTime}ms
                        </div>
                        <div class="detail-item">
                            <strong>Tests Passed:</strong> ${suite.passed}
                        </div>
                        <div class="detail-item">
                            <strong>Tests Failed:</strong> ${suite.failed}
                        </div>
                    </div>
                    
                    ${if (suite.errors.isNotEmpty()) generateErrorsSection(suite.errors) else ""}
                    
                    <div style="margin-top: 15px;">
                        <strong>Test Coverage:</strong> ${getTestCoverageDescription(suite.name)}
                    </div>
                </div>
            </div>
            """.trimIndent()
        }
    }

    private fun generateErrorsSection(errors: List<String>): String {
        return """
        <div class="errors">
            <h4>Errors and Failures:</h4>
            ${errors.joinToString("\n") { "<div class=\"error-item\">$it</div>" }}
        </div>
        """.trimIndent()
    }

    private fun generateRequirementsReport(requirements: Map<String, Boolean>): String {
        val requirementItems = requirements.map { (requirement, passed) ->
            val status = if (passed) "passed" else "failed"
            val icon = if (passed) "✓" else "✗"
            
            """
            <div class="requirement $status">
                <span class="requirement-icon $status">$icon</span>
                <span>$requirement</span>
            </div>
            """.trimIndent()
        }.joinToString("\n")

        return """
        <div class="requirements">
            <h2>Requirements Validation</h2>
            <p>Validation of all functional and non-functional requirements for the Chain messaging platform:</p>
            $requirementItems
        </div>
        """.trimIndent()
    }

    private fun getTestCoverageDescription(suiteName: String): String {
        return when {
            suiteName.contains("End-to-End") -> "Complete message flows, encryption integrity, status progression, network failure recovery"
            suiteName.contains("Integration") -> "Blockchain-P2P interaction, consensus mechanisms, message routing, network resilience"
            suiteName.contains("Security") -> "Signal Protocol implementation, key management, attack resistance, threat detection"
            suiteName.contains("Load") -> "High-volume messaging, large group distribution, concurrent calls, memory usage"
            else -> "Comprehensive testing coverage"
        }
    }

    fun saveReportToFile(htmlContent: String, filename: String = "test-report.html"): File {
        val reportsDir = File("app/build/reports/tests/comprehensive")
        reportsDir.mkdirs()
        
        val reportFile = File(reportsDir, filename)
        reportFile.writeText(htmlContent)
        
        return reportFile
    }

    data class ComprehensiveTestResults(
        val startTime: Long,
        val endTime: Long,
        val suiteResults: List<TestSuiteResult>,
        val requirementValidation: Map<String, Boolean>,
        val overallSuccess: Boolean,
        val totalPassed: Int,
        val totalFailed: Int,
        val successRate: Double
    )

    data class TestSuiteResult(
        val name: String,
        val passed: Int,
        val failed: Int,
        val errors: List<String>,
        val executionTime: Long
    )
}