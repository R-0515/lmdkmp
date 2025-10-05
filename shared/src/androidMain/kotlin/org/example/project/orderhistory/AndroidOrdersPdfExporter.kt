package org.example.project.orderhistory.report

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import org.example.project.orderhistory.domain.model.OrderHistoryUi
import org.example.project.orderhistory.ui.ExportResult
import org.example.project.orderhistory.ui.OrdersReportExporter
import org.example.project.orderhistory.ui.ReportMeta
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class AndroidOrdersPdfExporter(
    private val context: Context
) : OrdersReportExporter {

    @SuppressLint("SimpleDateFormat")
    override suspend fun export(
        orders: List<OrderHistoryUi>,
        meta: ReportMeta
    ): ExportResult {
        if (orders.isEmpty()) return ExportResult(false, error = "No orders to export")

        val cfg = PdfConfig(context)
        val style = PdfStyle(cfg.textSizePx)

        val doc = PdfDocument()
        var y = cfg.startY
        var pageIndex = 1
        var page = newPage(doc, cfg, style, pageIndex++, meta).also { y = it.second }
            .first

        orders.forEach { order ->
            if (y + cfg.lineH > cfg.pageHeight - cfg.margin) {
                doc.finishPage(page)
                page = newPage(doc, cfg, style, pageIndex++, meta).also { y = it.second }
                    .first
            }
            y = drawRow(page, cfg, style, order, y)
        }

        doc.finishPage(page)

        return runCatching {
            val outFile = writePdfToFile(doc)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
            ExportResult(true, uri.toString(), null)
        }.getOrElse {
            doc.close()
            ExportResult(false, null, it.message ?: "Export failed")
        }
    }

    // ---------- Drawing helpers (no app resources) ----------

    private fun newPage(
        doc: PdfDocument,
        cfg: PdfConfig,
        style: PdfStyle,
        pageIndex: Int,
        meta: ReportMeta
    ): Pair<PdfDocument.Page, Int> {
        val info = PdfDocument.PageInfo.Builder(cfg.pageWidth, cfg.pageHeight, pageIndex).create()
        val page = doc.startPage(info)
        val c = page.canvas
        var y = cfg.startY

        // Title
        drawText(c, meta.title.ifBlank { "Orders History" }, cfg.margin, y, style.bold)
        y += cfg.lineH

        // Meta (filter + generatedAt)
        drawText(c, meta.filterSummary, cfg.margin, y, style.normal)
        y += cfg.lineH
        drawText(c, meta.generatedAt, cfg.margin, y, style.normal)
        y += cfg.headerSpacing

        // Headers
        drawText(c, "No", cfg.margin, y, style.bold)
        drawText(c, "Customer", cfg.margin + cfg.colCustomer, y, style.bold)
        drawText(c, "Total", cfg.pageWidth - cfg.colTotal, y, style.bold)
        drawText(c, "Status", cfg.pageWidth - cfg.colStatus, y, style.bold)
        drawText(c, "Age", cfg.pageWidth - cfg.colAge, y, style.bold)
        y += cfg.lineH

        // Divider
        c.drawLine(
            cfg.margin.toFloat(), y.toFloat(),
            (cfg.pageWidth - cfg.margin).toFloat(), y.toFloat(),
            style.normal
        )
        y += cfg.headerSpacing

        return page to y
    }

    private fun drawRow(
        page: PdfDocument.Page,
        cfg: PdfConfig,
        style: PdfStyle,
        order: OrderHistoryUi,
        y: Int
    ): Int {
        val c = page.canvas
        drawText(c, order.number, cfg.margin, y, style.normal)
        drawText(c, order.customer, cfg.margin + cfg.colCustomer, y, style.normal)
        drawText(c, String.format(Locale.ROOT, "%.2f", order.total), cfg.pageWidth - cfg.colTotal, y, style.normal)
        drawText(
            c,
            order.status.name.lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) },
            cfg.pageWidth - cfg.colStatus, y, style.normal
        )
        drawText(c, relativeAge(order.createdAtMillis), cfg.pageWidth - cfg.colAge, y, style.normal)
        return y + cfg.lineH
    }

    private fun relativeAge(epochMs: Long): String {
        if (epochMs <= 0) return "-"
        val now = System.currentTimeMillis()
        val delta = (now - epochMs).coerceAtLeast(0)
        val min = 60_000L
        val hour = 60 * min
        val day = 24 * hour
        return when {
            delta < hour -> "${delta / min}m"
            delta < day -> "${delta / hour}h"
            delta < 7 * day -> "${delta / day}d"
            else -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(epochMs))
        }
    }

    private fun drawText(c: android.graphics.Canvas, text: String, x: Int, y: Int, paint: Paint) {
        c.drawText(text, x.toFloat(), y.toFloat(), paint)
    }

    private fun writePdfToFile(doc: PdfDocument): File {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val fileName = "orders_${sdf.format(Date())}.pdf"
        val outFile = File(context.cacheDir, fileName)
        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }

    // ---------- Local “resources” (computed sizes) ----------

    private class PdfStyle(textSizePx: Float) {
        val normal = Paint().apply {
            textSize = textSizePx
            isAntiAlias = true
            color = Color.BLACK
        }
        val bold = Paint().apply {
            textSize = textSizePx
            isAntiAlias = true
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    private class PdfConfig(context: Context) {
        private val dm = context.resources.displayMetrics

        // Convert helpers
        private fun dp(n: Int) = (n * dm.density).roundToInt()
        private fun sp(n: Float) = n * dm.scaledDensity

        // Page size (approx A4 @ 72dpi scaled to device density)
        val pageWidth: Int = (595 * dm.density).roundToInt()   // ~A4 width
        val pageHeight: Int = (842 * dm.density).roundToInt()  // ~A4 height

        // Layout
        val margin = dp(16)
        val lineH = dp(18)
        val headerSpacing = dp(10)

        // Columns (relative offsets)
        val colCustomer = dp(120)
        val colTotal = dp(160)
        val colStatus = dp(100)
        val colAge = dp(40)

        val textSizePx: Float = sp(12f)

        val startY: Int = margin
    }

    fun sharePdf(pdfUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF"))
    }
}
