package com.trobat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trobat.ui.theme.BackgroundPrincipal
import com.trobat.ui.theme.TrobatBackground
import com.trobat.ui.theme.TrobatCard
import com.trobat.ui.theme.TrobatOutline
import com.trobat.ui.theme.TrobatPurple
import com.trobat.ui.theme.TrobatText
import com.trobat.ui.theme.TrobatTextSecondary

@Composable
fun TermsAndConditionsDialog(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var isChecked by rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = TrobatBackground,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Encabezado fijo con branding
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundPrincipal)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Column {
                        Text(
                            text = "Términos y Condiciones de Uso",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TrobatBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Última actualización: Junio de 2026",
                            style = MaterialTheme.typography.bodySmall,
                            color = TrobatBackground.copy(alpha = 0.65f)
                        )
                    }
                }

                // Contenido desplazable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    TermsSection(number = 1, title = "Aceptación de los Términos")
                    TermsParagraph(
                        "Al descargar, instalar o utilizar la aplicación Trobat (en adelante, \"la Aplicación\"), " +
                        "usted (en adelante, el \"Usuario\") acepta quedar vinculado por estos Términos y Condiciones. " +
                        "Si usted no está de acuerdo con todas estas condiciones, no debe utilizar la Aplicación " +
                        "ni enviar información a través de ella."
                    )

                    TermsSection(number = 2, title = "Uso del Servicio y Envío de Reportes")
                    TermsParagraph("Para el envío de un Reporte, la Aplicación le ofrece dos modalidades de uso, quedando la elección estrictamente a su criterio y voluntad:")
                    TermsBullet(
                        label = "Reporte Anónimo:",
                        text = "Usted puede enviar un Reporte de manera completamente anónima. Bajo esta modalidad, " +
                               "la Aplicación no le requerirá la creación de una cuenta, registro previo ni la provisión " +
                               "de ningún dato que pueda revelar su identidad (como su nombre, DNI, teléfono o correo electrónico)."
                    )
                    TermsBullet(
                        label = "Opción de Registro y Contacto Voluntario:",
                        text = "Si usted desea colaborar de forma activa con la investigación y está dispuesto a recibir " +
                               "consultas de los organismos competentes, la Aplicación le brinda la opción de registrar sus " +
                               "datos de contacto de manera voluntaria. Al elegir esta opción, usted consiente explícitamente " +
                               "que las autoridades policiales o judiciales intervinientes puedan comunicarse con usted con el " +
                               "único propósito de ampliar o certificar la información provista en su Reporte."
                    )
                    TermsBullet(
                        label = "Uso Prohibido:",
                        text = "En ambas modalidades, usted se compromete a no utilizar la Aplicación para fines ilícitos, " +
                               "dañar a terceros, vulnerar derechos de propiedad intelectual, o intentar acceder sin autorización " +
                               "a los sistemas informáticos de la plataforma. Queda terminantemente prohibido el uso de la " +
                               "Aplicación para realizar bromas, acoso o vandalismo digital."
                    )

                    TermsSection(number = 3, title = "Propiedad Intelectual de la Aplicación")
                    TermsParagraph(
                        "Todo el contenido, diseño de interfaz, código fuente, logotipos y marcas de la Aplicación son " +
                        "propiedad exclusiva del equipo de desarrollo de Trobat o de sus licenciantes organizacionales. " +
                        "Usted no adquiere ningún derecho de propiedad intelectual por el uso de la aplicación."
                    )

                    TermsSection(number = 4, title = "Contenido Generado por el Usuario (Información y Fotos)")
                    TermsParagraph(
                        "Si usted utiliza la Aplicación para publicar, subir o compartir contenido (datos de localización, " +
                        "texto descriptivo o fotografías tomadas con la cámara de su dispositivo móvil, en adelante los \"Reportes\"):"
                    )
                    TermsBullet(
                        label = null,
                        text = "Usted declara que el material aportado es de su autoría y fue capturado en un espacio público."
                    )
                    TermsBullet(
                        label = null,
                        text = "Usted conserva los derechos sobre su contenido, pero otorga a Trobat y a las autoridades policiales " +
                               "y judiciales intervinientes una licencia gratuita, no exclusiva, perpetua e irrevocable para usar, " +
                               "mostrar, analizar y procesar dicha información con el único fin de investigar la localización de " +
                               "la persona desaparecida."
                    )
                    TermsBullet(
                        label = null,
                        text = "Nos reservamos el derecho de eliminar, bloquear o no dar curso a cualquier Reporte que infrinja " +
                               "estos términos o sea considerado inapropiado, inexacto o difuso a nuestra exclusiva discreción."
                    )

                    TermsSection(number = 5, title = "Responsabilidad por Falsedad de la Información")
                    TermsParagraph("Dada la naturaleza crítica de la búsqueda de personas y el impacto directo en la seguridad pública:")
                    TermsBullet(
                        label = null,
                        text = "Usted asume total responsabilidad por la veracidad y exactitud de los datos y fotografías que aporta."
                    )
                    TermsBullet(
                        label = null,
                        text = "Usted queda formalmente notificado de que el desvío intencionado de recursos policiales, la " +
                               "obstrucción de la justicia o la falsa denuncia constituyen delitos tipificados en el Código Penal " +
                               "de la Nación Argentina. Las autoridades judiciales competentes podrán iniciar las acciones legales " +
                               "correspondientes persiguiendo la traza tecnológica del dispositivo si se detectase un uso malicioso " +
                               "o de sabotaje contra el sistema."
                    )

                    TermsSection(number = 6, title = "Limitación de Responsabilidad y Exención de Garantías")
                    TermsParagraph(
                        "La Aplicación se proporciona \"tal cual\" y \"según disponibilidad\". Trobat no garantiza que el servicio " +
                        "sea ininterrumpido, libre de errores o que los Reportes enviados deriven en la localización inmediata " +
                        "de la persona buscada. En ninguna circunstancia los administradores de la plataforma serán responsables " +
                        "por daños directos, indirectos, incidentales o consecuentes derivados del uso o la imposibilidad de uso " +
                        "de la aplicación."
                    )

                    TermsSection(number = 7, title = "Modificaciones a los Términos")
                    TermsParagraph(
                        "Nos reservamos el derecho de modificar o actualizar estos Términos y Condiciones en cualquier momento. " +
                        "Los cambios entrarán en vigencia inmediatamente después de su publicación en la Aplicación. El uso " +
                        "continuado de la aplicación tras dichos cambios constituye su aceptación de los mismos."
                    )

                    TermsSection(number = 8, title = "Terminación")
                    TermsParagraph(
                        "Podemos suspender, limitar o cancelar su acceso a la Aplicación o a la función de envío de Reportes " +
                        "de forma inmediata, sin previo aviso ni responsabilidad, por cualquier motivo, incluido el uso indebido " +
                        "del sistema o el incumplimiento de estos Términos."
                    )

                    TermsSection(number = 9, title = "Ley Aplicable y Resolución de Disputas")
                    TermsParagraph(
                        "Estos términos se regirán e interpretarán de acuerdo con las leyes vigentes de la República Argentina " +
                        "(incluyendo la Ley 25.326 de Protección de Datos Personales). Cualquier disputa derivada de estos términos " +
                        "se resolverá exclusivamente en los tribunales ordinarios competentes de la Ciudad Autónoma de Buenos Aires."
                    )

                    TermsSection(number = 10, title = "Contacto")
                    TermsParagraph("Si tiene alguna pregunta sobre estos Términos y Condiciones, puede contactarse con los administradores del sistema en:")
                    TermsBullet(label = "Correo electrónico:", text = "soporte@trobat.com.ar")

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Pie de página fijo: checkbox + botones
                HorizontalDivider(color = TrobatOutline)
                Column(
                    modifier = Modifier
                        .background(TrobatCard)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isChecked = !isChecked }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = TrobatPurple,
                                uncheckedColor = TrobatTextSecondary
                            )
                        )
                        Text(
                            text = "He leído y acepto los Términos y Condiciones de uso",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TrobatText,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReject,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Rechazar",
                                color = TrobatTextSecondary
                            )
                        }
                        Button(
                            onClick = onAccept,
                            enabled = isChecked,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TrobatPurple,
                                disabledContainerColor = TrobatOutline
                            )
                        ) {
                            Text(text = "Aceptar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TermsSection(number: Int, title: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "$number. $title",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = TrobatText
    )
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun TermsParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = TrobatText,
        lineHeight = 20.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun TermsBullet(label: String?, text: String) {
    Row(
        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            color = TrobatPurple,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = buildAnnotatedString {
                if (label != null) {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append("$label ")
                    }
                }
                append(text)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TrobatText,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
