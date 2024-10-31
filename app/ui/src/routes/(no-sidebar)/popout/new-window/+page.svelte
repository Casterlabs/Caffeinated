<script>
	import PageTitle from '$lib/layout/PageTitle.svelte';
	import WidgetPreview from '$lib/WidgetPreview.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';

	import { page } from '$app/stores';
	import { onMount } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Window Popout');

	let widget;
	let previewOpacity = 1;

	onMount(() => {
		const id = $page.url.searchParams.get('id');

		Caffeinated.pluginIntegration.widgets
			.then((widgets) => {
				// Filter for a widget object with a matching id.
				// This'll return `undefined` if there's no matching result.
				return widgets.filter((w) => w.id == id)[0];
			})
			.then((w) => {
				// All is good.
				widget = w;
			});
	});
</script>

<svelte:head>
	<style>
		.dock-widget-preview {
			position: absolute;
			top: 0;
			left: 0;
			width: 100vw !important;
			height: 100vh !important;
		}
	</style>
</svelte:head>

<PageTitle title="co.casterlabs.caffeinated.app.popout_window" />

{#if widget}
	<span style:--preview-opacity={previewOpacity}>
		<WidgetPreview {widget} mode="DOCK" />
	</span>
{/if}
