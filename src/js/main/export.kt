import com.earthapp.shovel.Document
import com.earthapp.shovel.fetchDocument
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

/**
 * Fetches a document from the given URL.
 * @return The document from the page.
 */
@OptIn(DelicateCoroutinesApi::class, ExperimentalJsExport::class)
@JsExport
fun String.fetchDocumentAsPromise(): Promise<Document> = GlobalScope.promise { fetchDocument() }

/**
 * Fetches a document from the given URL.
 * @return The document from the page.
 */
@OptIn(DelicateCoroutinesApi::class, ExperimentalJsExport::class)
@JsExport
fun fetchAsPromise(url: String): Promise<Document> = url.fetchDocumentAsPromise()