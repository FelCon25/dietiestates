package it.unina.dietiestates.features.property.presentation._compontents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Elevator
import androidx.compose.material.icons.outlined.EnergySavingsLeaf
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.RealEstateAgent
import androidx.compose.material.icons.outlined.Stairs
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import it.unina.dietiestates.BuildConfig
import it.unina.dietiestates.R
import it.unina.dietiestates.features.agency.domain.getEmptyAgency
import it.unina.dietiestates.features.property.domain.InsertionType
import it.unina.dietiestates.features.property.domain.Property
import it.unina.dietiestates.features.property.domain.PropertyCondition
import it.unina.dietiestates.features.property.domain.PropertyType
import it.unina.dietiestates.ui.theme.Green80
import java.text.NumberFormat

@Composable
fun PropertyItem(
    modifier: Modifier = Modifier,
    property: Property
) {

    val numberFormat = remember { NumberFormat.getNumberInstance(java.util.Locale.ITALY) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4 / 3f)
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = "${BuildConfig.BASE_URL}${property.images.firstOrNull()}",
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )

                Text(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Green80, shape = ShapeDefaults.Small)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    text = "SELL",
                    color = Color.White,
                    fontSize = 12.sp
                )

                Text(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = .35f), shape = ShapeDefaults.Small)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    text = property.propertyCondition.name.replace('_', ' '),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "€ ${numberFormat.format(property.price)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        if(property.airConditioning){
                            Icon(
                                imageVector = Icons.Outlined.AcUnit,
                                contentDescription = null
                            )
                        }

                        if(property.elevator){
                            Icon(
                                imageVector = Icons.Outlined.Elevator,
                                contentDescription = null
                            )
                        }

                        if(property.concierge){
                            Image(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.concierge),
                                contentDescription = ""
                            )
                        }

                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)){
                            append(property.propertyType.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                        append(" in ")
                        append("${property.city},")

                        append(" ${property.address}")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AspectRatio,
                            contentDescription = null
                        )

                        Text(text = "${property.surfaceArea} m²")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MeetingRoom,
                            contentDescription = null
                        )

                        Text(text = "${property.rooms}")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Stairs,
                            contentDescription = null
                        )

                        Text(text = "${property.floors}")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EnergySavingsLeaf,
                            contentDescription = null
                        )

                        Text(text = property.energyClass)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RealEstateAgent, contentDescription = "Agency icon",
                        //tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = property.agency.businessName,
                        fontWeight = FontWeight.SemiBold,
                        //color = Color.White
                    )
                }
            }

        }
    }
}

@Preview
@Composable
private fun Preview() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        PropertyItem(
            modifier = Modifier,
            property = Property(
                propertyId = 1,
                agencyId = 1,
                description = "Casa molto bella",
                price = 100000.0,
                surfaceArea = 100,
                rooms = 3,
                floors = 2,
                elevator = true,
                energyClass = "A",
                concierge = true,
                airConditioning = true,
                furnished = true,
                propertyType = PropertyType.VILLA,
                insertionType = InsertionType.SALE,
                address = "Via Roma 69",
                city = "Roma",
                postalCode = "00100",
                province = "Roma",
                country = "Italia",
                latitude = 41.890251,
                longitude = 12.492373,
                images = listOf(),
                agentId = 1,
                propertyCondition = PropertyCondition.NEW,
                createdAt = "2022-01-01",
                agency = getEmptyAgency().copy(businessName = "Dietiestats")
            )
        )

    }

}