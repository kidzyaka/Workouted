package com.kidz.workouted.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kidz.workouted.R
import com.kidz.workouted.presentation.components.WheelPicker

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val height by viewModel.heightCm.collectAsState()
    val weight by viewModel.weightKg.collectAsState()
    val age by viewModel.age.collectAsState()

    val heights = (100..250).map { it.toString() }
    val weights = (30..200).map { it.toString() }
    val ages = (14..100).map { it.toString() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.welcome_to_workouted),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.set_parameters_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.height_cm),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                WheelPicker(
                    items = heights,
                    initialIndex = heights.indexOf(height.toInt().toString()),
                    onItemSelected = { index ->
                        viewModel.setHeight(heights[index].toDouble())
                    },
                    modifier = Modifier.width(70.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.weight_kg_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                WheelPicker(
                    items = weights,
                    initialIndex = weights.indexOf(weight.toInt().toString()),
                    onItemSelected = { index ->
                        viewModel.setWeight(weights[index].toDouble())
                    },
                    modifier = Modifier.width(70.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.age),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                WheelPicker(
                    items = ages,
                    initialIndex = ages.indexOf(age.toString()),
                    onItemSelected = { index ->
                        viewModel.setAge(ages[index].toInt())
                    },
                    modifier = Modifier.width(70.dp)
                )
            }
        }

        Button(
            onClick = {
                viewModel.completeOnboarding(onComplete)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = stringResource(R.string.get_started),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
