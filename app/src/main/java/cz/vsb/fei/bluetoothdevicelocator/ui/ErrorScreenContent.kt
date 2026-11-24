package cz.vsb.fei.bluetoothdevicelocator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cz.vsb.fei.bluetoothdevicelocator.R

/**
 * Defines content for screens in case of errors.
 * @param errorMessage Message to show.
 * @param modifier Modifier to apply to the layout.
 */
@Composable
fun ErrorScreenContent(
    errorMessage: String,
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().padding(48.dp)
    ) {
        Text(
            text = stringResource(R.string.device_not_found_caption),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(32.dp).size(128.dp)
        )

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
    }
}