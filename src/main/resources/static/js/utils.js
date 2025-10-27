// =============================================================================
// SHARED UTILITY FUNCTIONS
// =============================================================================

// Formats byte size to appropriate unit (B, KB, MB, GB, TB)
function formatSize(bytes) {
    if (bytes === 0) return '0 B';
    
    const units = ['B', 'KB', 'MB', 'GB', 'TB'];
    const kilobyte = 1024;
    const unitIndex = Math.floor(Math.log(bytes) / Math.log(kilobyte));
    
    return parseFloat((bytes / Math.pow(kilobyte, unitIndex)).toFixed(2)) + ' ' + units[unitIndex];
}
